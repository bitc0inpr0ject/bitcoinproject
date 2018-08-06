package BitcoinService;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import javafx.util.Pair;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BitcoinUtils {

    private static BitcoinClient bitcoinClient = null;
    public static void createBitcoinClientInstance(NetworkParameters params, String server, String username, String password) {
        if (bitcoinClient == null) {
            try {
                URI uri = new URI(server);
                System.out.println("bitcoinclient networkID" + params.getId());
                System.out.println("uri server bitcoin: " + uri.toString());
                bitcoinClient = new BitcoinClient(params, uri, username, password);
            } catch (Exception e) {
                System.out.println("cannot connect to server and start bitcoinClient");
                e.printStackTrace();
                bitcoinClient = null;
            }
        }
    }
    public static BitcoinClient getBitcoinClientInstance() {
        return bitcoinClient;
    }
    public static List<Transaction> getTransactionInBlock(BitcoinClient client, int currentBlock) throws IOException {
        return client.getBlock(currentBlock).getTransactions();
    }
    public static Transaction getTransaction(BitcoinClient client, String sha256Hash) throws IOException {
        return client.getRawTransaction(Sha256Hash.wrap(sha256Hash));
    }
    public static int getBlockCount(BitcoinClient client) throws IOException {
        return client.getBlockCount();
    }

    public static List<TransactionInput> getTransactionInputInBlock(BitcoinClient client, int currentBlock) throws IOException {
        List<TransactionInput> transactionInputs = new ArrayList<>();
        List<Transaction> transactions = getTransactionInBlock(client,currentBlock);
        for (Transaction tx :
                transactions) {
            transactionInputs.addAll(tx.getInputs());
        }
        return transactionInputs;
    }
    public static List<TransactionOutput> getTransactionOutputInBlock(BitcoinClient client, int currentBlock) throws IOException {
        List<TransactionOutput> transactionOutputs = new ArrayList<>();
        List<Transaction> transactions = getTransactionInBlock(client,currentBlock);
        for (Transaction tx :
                transactions) {
            transactionOutputs.addAll(tx.getOutputs());
        }
        return transactionOutputs;
    }

    public static boolean checkTxOutputInTxInputs(List<TransactionInput> txInputs, String txHash, int index) {
        for (TransactionInput txInp :
                txInputs) {
            try {
                if (txInp.getOutpoint().getHash().toString().equals(txHash)
                        && txInp.getOutpoint().getIndex() == index)
                    return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    public static List<TransactionOutput> getTransactionOutputByAddress(NetworkParameters params, List<TransactionOutput> txOutputs, Address address) {
        List<TransactionOutput> txOutputsByAddress = new ArrayList<>();
        for (TransactionOutput txOutput :
                txOutputs) {
            try {
                if (txOutput.getScriptPubKey().getToAddress(params).toString().equals(address.toString()))
                    txOutputsByAddress.add(txOutput);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return txOutputsByAddress;
    }
    public static Coin getAmt(List<TransactionOutput> txOutputs) {
        Coin res = Coin.ZERO;
        for (TransactionOutput txOutput :
                txOutputs) {
            try {
                res = res.add(txOutput.getValue());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return res;
    }

    private static void signTx(NetworkParameters params, Transaction rawTx, List<ECKey> privKeys) {
        for (int i = 0; i < rawTx.getInputs().size(); i++) {
            TransactionInput txInput = rawTx.getInput(i);
            Script scriptSig = ScriptBuilder.createInputScript(rawTx.calculateSignature(
                        i,
                        privKeys.get(i),
                        ScriptBuilder.createOutputScript(privKeys.get(i).toAddress(params)),
                        Transaction.SigHash.ALL,
                        false),
                    privKeys.get(i));
            txInput.setScriptSig(scriptSig);
        }
    }
    private static void canAddChangeOutput(NetworkParameters params, Transaction rawTx, List<ECKey> privKeys, Address changeAddress, Coin feePerKb) {
        Transaction tmpTx = new Transaction(params,rawTx.bitcoinSerialize());
        Coin maxFee = Coin.ZERO;
        TransactionOutput txChangeOutput = tmpTx.addOutput(Coin.ZERO,changeAddress);
        while (true) {
            signTx(params,tmpTx,privKeys);
            Coin fee = feePerKb.multiply(tmpTx.getMessageSize()).divide(1000L);
            if (fee.isGreaterThan(maxFee)) maxFee = fee;
            else break;
            if (rawTx.getInputSum().isLessThan(rawTx.getOutputSum().add(maxFee)))
                return;
            txChangeOutput.setValue(rawTx.getInputSum().subtract(rawTx.getOutputSum().add(maxFee)));
            if (txChangeOutput.getValue().isLessThan(txChangeOutput.getMinNonDustValue(feePerKb)))
                return;
        }
        rawTx.addOutput(txChangeOutput);
        signTx(params,rawTx,privKeys);
    }
    public static Transaction sendTx(BitcoinClient client, Map<TransactionOutput,ECKey> originalInputs, List<Pair<Address,Coin>> candidates, Address changeAddress, Coin feePerKb) throws InsufficientMoneyException, IOException {
        NetworkParameters params = client.getNetParams();
        Transaction rawTx = new Transaction(params);

        for (Pair<Address, Coin> candidate:
             candidates) {
            rawTx.addOutput(candidate.getValue(), candidate.getKey());
        }

        for (TransactionOutput originalInput:
             originalInputs.keySet()) {
            rawTx.addInput(originalInput);
        }
        List<ECKey> privKeys = new ArrayList<>();
        for (int i = 0; i < rawTx.getInputs().size(); i++) {
            privKeys.add(originalInputs.get(rawTx.getInput(i).getConnectedOutput()));
        }
        signTx(params,rawTx,privKeys);

        Coin fee = feePerKb.multiply(rawTx.getMessageSize()).divide(1000L);

        if (rawTx.getInputSum().isLessThan(rawTx.getOutputSum().add(fee)))
            throw new InsufficientMoneyException(rawTx.getOutputSum().add(fee).subtract(rawTx.getInputSum()));

        canAddChangeOutput(params,rawTx,privKeys,changeAddress,feePerKb);

        for (TransactionInput txInp :
                rawTx.getInputs()) {
            txInp.verify();
        }

        client.sendRawTransaction(rawTx);
        return rawTx;
    }
    public static Transaction sendToAddressesByPrivKey(BitcoinClient client, List<TransactionOutput> unspentTxOutputs, ECKey privKey, List<Pair<Address,Coin>> candidates, Coin feePerKb) throws InsufficientMoneyException, IOException {
        NetworkParameters params = client.getNetParams();
        Map<TransactionOutput,ECKey> originalInputs = new HashMap<>();
        Coin insufficientMoney = Coin.ZERO;
        for (TransactionOutput utxo :
                unspentTxOutputs) {
            try {
                if(utxo.getScriptPubKey().getToAddress(params) == null)
                    continue;
                if (!utxo.getScriptPubKey().getToAddress(params).toString().equals(privKey.toAddress(params).toString()))
                    continue;
                originalInputs.put(utxo,privKey);
                return sendTx(client,originalInputs,candidates,privKey.toAddress(params),feePerKb);
            } catch (InsufficientMoneyException e) {
                insufficientMoney = e.missing;
            } catch (IOException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        throw new InsufficientMoneyException(insufficientMoney);
    }

    /**
     * Return the redeem script which is created from 2 client public keys and 1 server public key.
     * This is the 2-of-3 multisig.
     * Use at the server side.
     * Use the redeem script to create the multisig address and signature script, estimate the fee.
     * To export public key from ECKey, we can use method: ECKey.getPubKey(): byte[]
     * To import public key, we can use function: ECKey.fromPublicOnly(byte[] pub): ECKey
     * IMPORTANT:
     * The script created from (A,B,C) is different from the one created from (B,A,C).
     * So remember the order of the public keys.
     * @param clientPubKey_1    the first client public key
     * @param clientPubKey_2    the second client public key
     * @param serverPubKey      the server public key
     * @return                  the redeem script created from 3 public keys
     */
    public static Script create2of3MultiSigRedeemScript(ECKey clientPubKey_1, ECKey clientPubKey_2, ECKey serverPubKey) {
        List<ECKey> pubkeys = new ArrayList<>();
        pubkeys.add(clientPubKey_1);
        pubkeys.add(clientPubKey_2);
        pubkeys.add(serverPubKey);
        return ScriptBuilder.createMultiSigOutputScript(2, pubkeys);
    }

    /**
     * Return the address which is created from the redeem script.
     * This is the 2-of-3 multisig.
     * Use at the server side.
     * Use the address to be able to receive coins from other addresses.
     * @param params                    the network parameter (MainNet, TestNet, ...)
     * @param script2of3MultiSigRedeem  the redeem script
     * @return                          the address created from the redeem script
     */
    public static Address create2of3MultiSigAddress(NetworkParameters params, Script script2of3MultiSigRedeem) {
        return Address.fromP2SHScript(params,ScriptBuilder.createP2SHOutputScript(script2of3MultiSigRedeem));
    }

    private static Coin estimateFee(Transaction rawTx, Script script2of3MultiSigRedeem, Coin feePerKb) {
        int sz = 0;
        int maxSz = 0;
        for (TransactionOutput txOutput :
                rawTx.getOutputs()) {
            sz += txOutput.getMessageSize();
            if (txOutput.getMessageSize() > maxSz)
                maxSz = txOutput.getMessageSize();
        }
        sz += maxSz;
        for (TransactionInput txInput :
                rawTx.getInputs()) {
            sz += txInput.getMessageSize() + script2of3MultiSigRedeem.getProgram().length + 100*2;
        }
        return feePerKb.multiply(sz).divide(1000L);
    }

    /**
     * Return the raw transaction which has transaction outputs and unsigned transaction inputs
     * This is the 2-of-3 multisig.
     * Use at the server side.
     * IMPORTANT:
     * 1 KB = 1000 bytes
     * RECOMMENDED:
     * feePerKb = Coin.valueOf(1000)        1000 satoshis/KB
     * @param params                        the network parameter (MainNet, TestNet, ...)
     * @param unspentTxOutputs              the list of unspent transaction outputs
     * @param script2of3MultiSigRedeem      the redeem script
     * @param candidates                    the list of <Address,Coin> pairs which Coin is the amount we want to send to Address
     * @param feePerKb                      the price of transaction (fee per KB)
     * @return                              the raw transaction
     * @throws InsufficientMoneyException   when the input sum is not enough to send, it will throw this exception
     */
    public static Transaction create2of3MultiSigRawTx(NetworkParameters params, List<TransactionOutput> unspentTxOutputs, Script script2of3MultiSigRedeem, List<Pair<Address,Coin>> candidates, Coin feePerKb) throws InsufficientMoneyException {
        Transaction rawTx = new Transaction(params);
        Coin maxMinNonDustValue = Coin.ZERO;
        for (Pair<Address, Coin> candidate :
                candidates) {
            TransactionOutput txOut = rawTx.addOutput(candidate.getValue(),candidate.getKey());
            if (maxMinNonDustValue.isLessThan(txOut.getMinNonDustValue(feePerKb)))
                maxMinNonDustValue = txOut.getMinNonDustValue(feePerKb);
        }
        for (TransactionOutput utxo :
                unspentTxOutputs) {
            if(utxo.getScriptPubKey().getToAddress(params) == null)
                continue;
            if (!utxo.getScriptPubKey().getToAddress(params).toString().equals(
                    create2of3MultiSigAddress(params,script2of3MultiSigRedeem).toString()))
                continue;
            rawTx.addInput(utxo);
            if (rawTx.getInputSum().isGreaterThan(rawTx.getOutputSum().add(estimateFee(rawTx,script2of3MultiSigRedeem,feePerKb))))
                break;
        }
        if (rawTx.getInputSum().isLessThan(rawTx.getOutputSum().add(estimateFee(rawTx,script2of3MultiSigRedeem,feePerKb))))
            throw new InsufficientMoneyException(rawTx.getOutputSum().add(estimateFee(rawTx,script2of3MultiSigRedeem,feePerKb)).subtract(rawTx.getInputSum()));
        if (rawTx.getInputSum().isGreaterThan(rawTx.getOutputSum()
                .add(estimateFee(rawTx,script2of3MultiSigRedeem,feePerKb))
                .add(maxMinNonDustValue)))
            rawTx.addOutput(rawTx.getInputSum().subtract(rawTx.getOutputSum()
                    .add(estimateFee(rawTx,script2of3MultiSigRedeem,feePerKb)))
                    ,create2of3MultiSigAddress(params,script2of3MultiSigRedeem));
        return rawTx;
    }

    /**
     * Return the list of transaction hashes.
     * This is the 2-of-3 multisig.
     * Use at the server side.
     * To be able to send the transaction, the client MUST sign them.
     * @param rawTx                     the raw transaction after created by the create2of3MultiSigRawTx function.
     * @param script2of3MultiSigRedeem  the redeem script
     * @return                          the list of transaction hashes
     */
    public static List<Sha256Hash> create2of3MultiSigRawTxHash(Transaction rawTx, Script script2of3MultiSigRedeem) {
        List<Sha256Hash> sha256Hashes = new ArrayList<>();
        for (int i = 0; i < rawTx.getInputs().size(); i++) {
            sha256Hashes.add(rawTx.hashForSignature(i, script2of3MultiSigRedeem, Transaction.SigHash.ALL, false));
        }
        return sha256Hashes;
    }

    /**
     * Return the list of transaction signatures.
     * This is the 2-of-3 multisig.
     * Use at the client side.
     * After receiving the list of transaction hashes, the client MUST sign them to create the list of transaction signatures.
     * After signing them, the client MUST send the list of the signatures to the server.
     * To export private key from ECKey, we can use method:
     *      ECKey.getPrivateKeyEncoded(NetworkParameters params).toBase58(): String
     *      with:   NetworkParameters params: the network parameter (MainNet, TestNet, ...)
     * To import private key, we can use function:
     *      DumpedPrivateKey.fromBase58(NetworkParameters params, String base58): ECKey
     *      with:   NetworkParameters params: the network parameter (MainNet, TestNet, ...)
     *              String base58: the private key in String format
     * @param rawTxHashes   the list of transaction hashes
     * @param privKey       the private key
     * @return              the list of transaction signatures.
     */
    public static List<TransactionSignature> create2of3MultiSigTxSig(List<Sha256Hash> rawTxHashes, ECKey privKey) {
        List<TransactionSignature> txSigs = new ArrayList<>();
        for (Sha256Hash txHash :
                rawTxHashes) {
            txSigs.add(new TransactionSignature(privKey.sign(txHash), Transaction.SigHash.ALL, false));
        }
        return txSigs;
    }

    /**
     * Complete the transaction.
     * This is the 2-of-3 multisig.
     * Use at the server side.
     * After receiving the list of transaction signatures, the server will add them and complete the transaction.
     * IMPORTANT:
     * Complete the transaction, but not send the transaction to the network.
     * To send the transaction to the network, use the method:
     *      BitcoinClient.sendRawTransaction(Transaction tx): Sha256Hash
     * @param rawTx                     the raw transaction
     * @param script2of3MultiSigRedeem  the redeem script
     * @param userTxSign                the list of transaction signatures
     * @param serverKey                 the server private key
     * @return                          the completed transaction
     */
    public static Transaction signRaw2of3MultiSigTransaction(Transaction rawTx, Script script2of3MultiSigRedeem, List<TransactionSignature> userTxSign, ECKey serverKey) {
        for (int i = 0; i < rawTx.getInputs().size(); i++) {
            List<TransactionSignature> txSignatures = new ArrayList<>();
            txSignatures.add(userTxSign.get(i));
            txSignatures.add(rawTx.calculateSignature(i,serverKey,script2of3MultiSigRedeem,Transaction.SigHash.ALL,false));
            rawTx.getInput(i).setScriptSig(ScriptBuilder.createP2SHMultiSigInputScript(txSignatures,script2of3MultiSigRedeem));
            rawTx.getInput(i).verify();
        }
        return rawTx;
    }

    public static void getNotify(MongoTemplate mongoTemplate, BitcoinClient client, int confirmations) {
        Thread secondaryThread = new Thread(new Runnable() {
            public void run()
            {
                MongoTemplate _mongoTemplate = mongoTemplate;
                BitcoinClient _client = client;
                int _confirmations = confirmations;
                ServerSocket echoServer = null;
                try {
                    echoServer = new ServerSocket(9999);
                }
                catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    while (true) {
                        Socket clientSocket = echoServer.accept();
                        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                        DataInputStream is = new DataInputStream(clientSocket.getInputStream());
                        out.println("Halo");
                        while (true) {
                            if (out.checkError()) {
                                System.out.println("ERROR writing data to socket !!!");
                                break;
                            }
                            String mess = is.readLine();
                            if (mess != null){
                                int _block = Integer.parseInt(mess);
                                if (_block > 0) {
                                    clientSocket.close();
                                    System.out.println(_block);
                                    updateDB(_mongoTemplate,_client,_block,_confirmations);
                                }
                            }
                            else
                                break;
                            out.println("Ok!");
                        }
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }});
        secondaryThread.start();
    }
    public static void updateDB(MongoTemplate mongoTemplate, BitcoinClient client, int currentBlock, int confirmations){
        try {
            BitcoinAddressService.update(mongoTemplate,client,currentBlock,confirmations);
            BitcoinWalletService.update(mongoTemplate,client,currentBlock,confirmations);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
