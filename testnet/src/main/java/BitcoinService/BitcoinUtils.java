package BitcoinService;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import javafx.util.Pair;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.io.IOException;
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
    public static List<Transaction> getTransactionInBlock(int currentBlock) throws IOException {
        return bitcoinClient.getBlock(currentBlock).getTransactions();
    }
    public static Transaction getTransaction(String sha256Hash) throws IOException {
        return getBitcoinClientInstance().getRawTransaction(Sha256Hash.wrap(sha256Hash));
    }
    public static int getBlockCount() throws IOException {
        return bitcoinClient.getBlockCount();
    }

    public static List<TransactionInput> getTransactionInputInBlock(int currentBlock) throws IOException {
        List<TransactionInput> transactionInputs = new ArrayList<>();
        List<Transaction> transactions = getTransactionInBlock(currentBlock);
        for (Transaction tx :
                transactions) {
            transactionInputs.addAll(tx.getInputs());
        }
        return transactionInputs;
    }
    public static List<TransactionOutput> getTransactionOutputInBlock(int currentBlock) throws IOException {
        List<TransactionOutput> transactionOutputs = new ArrayList<>();
        List<Transaction> transactions = getTransactionInBlock(currentBlock);
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
            } catch (Exception ignore) { }
        }
        return false;
    }
    public static List<TransactionOutput> getTransactionOutputByAddress(List<TransactionOutput> txOutputs, Address addr) {
        List<TransactionOutput> txOutputsByAddress = new ArrayList<>();
        for (TransactionOutput txOut :
                txOutputs) {
            try {
                if (txOut.getAddressFromP2PKHScript(bitcoinClient.getNetParams()).toString().equals(addr.toString()))
                    txOutputsByAddress.add(txOut);
            } catch (Exception ignore) { }
        }
        return txOutputsByAddress;
    }
    public static Coin getAmt(List<TransactionOutput> txOutputs) {
        Coin res = Coin.ZERO;
        for (TransactionOutput txOut :
                txOutputs) {
            try {
                res = res.add(txOut.getValue());
            } catch (Exception ignore) { }
        }
        return res;
    }

    private static void signTx(Transaction rawTx, List<ECKey> privKeys) {
        for (int i = 0; i < rawTx.getInputs().size(); i++) {
            TransactionInput txInp = rawTx.getInput(i);
            Script scriptSig = ScriptBuilder.createInputScript(rawTx.calculateSignature(
                        i,
                        privKeys.get(i),
                        ScriptBuilder.createOutputScript(privKeys.get(i).toAddress(bitcoinClient.getNetParams())),
                        Transaction.SigHash.ALL,
                        false),
                    privKeys.get(i));
            txInp.setScriptSig(scriptSig);
        }
    }
    private static void canAddChangeOutput(Transaction rawTx, List<ECKey> privKeys, Address changeAddress, Coin feePerKb) {
        Transaction tmpTx = new Transaction(bitcoinClient.getNetParams(),rawTx.bitcoinSerialize());
        Coin maxFee = Coin.ZERO;
        TransactionOutput txChangeOutput = tmpTx.addOutput(Coin.ZERO,changeAddress);
        while (true) {
            signTx(tmpTx,privKeys);
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
        signTx(rawTx,privKeys);
    }
    public static Transaction sendTx(Map<TransactionOutput,ECKey> originalInputs, List<Pair<Address,Coin>> candidates, Address changeAddress, Coin feePerKb) throws InsufficientMoneyException, IOException {
        Transaction rawTx = new Transaction(bitcoinClient.getNetParams());

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
        signTx(rawTx,privKeys);

        Coin fee = feePerKb.multiply(rawTx.getMessageSize()).divide(1000L);

        if (rawTx.getInputSum().isLessThan(rawTx.getOutputSum().add(fee)))
            throw new InsufficientMoneyException(rawTx.getOutputSum().add(fee).subtract(rawTx.getInputSum()));

        canAddChangeOutput(rawTx,privKeys,changeAddress,feePerKb);

        for (TransactionInput txInp :
                rawTx.getInputs()) {
            txInp.verify();
        }

        bitcoinClient.sendRawTransaction(rawTx);
        return rawTx;
    }
    public static Transaction sendToAddressesByPrivKey(List<TransactionOutput> unspentTxOutputs, ECKey privKey, List<Pair<Address,Coin>> candidates, Coin feePerKb) throws InsufficientMoneyException, IOException {
        Map<TransactionOutput,ECKey> originalInputs = new HashMap<>();
        Coin insufficientMoney = Coin.ZERO;
        for (TransactionOutput utxo :
                unspentTxOutputs) {
            try {
                if(utxo.getAddressFromP2PKHScript(bitcoinClient.getNetParams()) == null)
                    continue;
                if (!utxo.getAddressFromP2PKHScript(bitcoinClient.getNetParams()).toString().equals(privKey.toAddress(bitcoinClient.getNetParams()).toString()))
                    continue;
                originalInputs.put(utxo,privKey);
                return sendTx(originalInputs,candidates,privKey.toAddress(bitcoinClient.getNetParams()),feePerKb);
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

    public static Script create2of3MultiSigRedeemScript(ECKey clientPubKey_1, ECKey clientPubKey_2, ECKey serverPubKey) {
        List<ECKey> pubkeys = new ArrayList<>();
        pubkeys.add(clientPubKey_1);
        pubkeys.add(clientPubKey_2);
        pubkeys.add(serverPubKey);
        return ScriptBuilder.createMultiSigOutputScript(2, pubkeys);
    }
    public static Address create2of3MultiSigAddress(Script script2of3MultiSigRedeem) {
        return Address.fromP2SHScript(bitcoinClient.getNetParams(),ScriptBuilder.createP2SHOutputScript(script2of3MultiSigRedeem));
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
    public static Transaction create2of3MultiSigRawTx(List<TransactionOutput> unspentTxOutputs, Script script2of3MultiSigRedeem, List<Pair<Address,Coin>> candidates, Coin feePerKb) throws InsufficientMoneyException {
        Transaction rawTx = new Transaction(bitcoinClient.getNetParams());
        Coin maxMinNonDustValue = Coin.ZERO;
        for (Pair<Address, Coin> candidate :
                candidates) {
            TransactionOutput txOut = rawTx.addOutput(candidate.getValue(),candidate.getKey());
            if (maxMinNonDustValue.isLessThan(txOut.getMinNonDustValue(feePerKb)))
                maxMinNonDustValue = txOut.getMinNonDustValue(feePerKb);
        }
        for (TransactionOutput utxo :
                unspentTxOutputs) {
            if(utxo.getAddressFromP2SH(bitcoinClient.getNetParams()) == null)
                continue;
            if (!utxo.getAddressFromP2SH(bitcoinClient.getNetParams()).toString().equals(
                    create2of3MultiSigAddress(script2of3MultiSigRedeem).toString()))
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
                    ,create2of3MultiSigAddress(script2of3MultiSigRedeem));
        return rawTx;
    }
    public static List<Sha256Hash> create2of3MultiSigRawTxHash(Transaction rawTx, Script script2of3MultiSigRedeem) {
        List<Sha256Hash> sha256Hashes = new ArrayList<>();
        for (int i = 0; i < rawTx.getInputs().size(); i++) {
            sha256Hashes.add(rawTx.hashForSignature(i, script2of3MultiSigRedeem, Transaction.SigHash.ALL, false));
        }
        return sha256Hashes;
    }
    public static List<TransactionSignature> create2of3MultiSigTxSig(List<Sha256Hash> rawTxHashes, ECKey privKey) {
        List<TransactionSignature> txSigs = new ArrayList<>();
        for (Sha256Hash txHash :
                rawTxHashes) {
            txSigs.add(new TransactionSignature(privKey.sign(txHash), Transaction.SigHash.ALL, false));
        }
        return txSigs;
    }
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

}
