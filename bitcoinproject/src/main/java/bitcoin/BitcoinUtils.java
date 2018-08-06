package bitcoin;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.jsonrpc.JsonRPCStatusException;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet2Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;
import org.springframework.data.util.Pair;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class BitcoinUtils {
    private String ServerAddress = "";
    private String RpcUsername = "";
    private String RpcPassword = "";
    private BitcoinClient bitcoinClient;
    private NetworkParameters networkParameters;

    /**
     * @return BitcoinClient Object
     */
    public BitcoinClient getClientInstance() {
        if (this.bitcoinClient == null) {
            try {
                URI uri;
                uri = new URI(this.ServerAddress);
                System.out.println("bitcoinclient networkID: " + this.networkParameters.getId());
                System.out.println("uri server bitcoin: " + uri.toString());
                this.bitcoinClient = new BitcoinClient(this.networkParameters, uri, this.RpcUsername, this.RpcPassword);
                return this.bitcoinClient;
            } catch (URISyntaxException e) {
                e.printStackTrace();
                this.bitcoinClient = null;
            }
        }
        return this.bitcoinClient;
    }

    public NetworkParameters getNetworkParameters() {
        return this.networkParameters;
    }

    /**
     * Generate BitcoinClient
     * @param network "testnet", "mainnet", "regtest"
     * @param server Inet4address and port, i.e. "http://192.168.1.38:18332"
     * @param rpcUsername rpcuser in bitcoin.conf (in full node)
     * @param rpcPassword rpcpassword in bitcoin.conf (in full node)
     */
    public void Generator(String network, String server, String rpcUsername, String rpcPassword) {
        this.ServerAddress = server;
        this.RpcUsername = rpcUsername;
        this.RpcPassword = rpcPassword;

        network = network.toLowerCase();
        switch (network) {
            case "testnet":
                this.networkParameters = new TestNet2Params();
                break;
            case "mainnet":
                this.networkParameters = new MainNetParams();
                break;
            case "regtest":
                this.networkParameters = new RegTestParams();
                break;
            default:
                this.networkParameters = new TestNet2Params();
                break;
        }
        this.bitcoinClient = this.getClientInstance();
    }

    /*public Transaction getTransaction(Sha256Hash hash) throws ExecutionException {
        while (true) {
            ExecutorService executor = Executors.newCachedThreadPool();
            Callable<Transaction> task = () -> this.getClientInstance().getRawTransaction(hash);
            Future<Transaction> future = executor.submit(task);
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException ex) {
                System.out.println("can not get transaction detail, retrying...");
                ex.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw e;
            } finally {
                future.cancel(true);
            }
        }
    }*/

    /**
     * Get all transaction in block by block index
     * @param currentBlock block index
     * @return List of Transaction in block
     * @throws ExecutionException
     */
    public List<Transaction> getTransactionInBlock(int currentBlock) throws ExecutionException {
        while (true) {
            ExecutorService executor = Executors.newCachedThreadPool();
            Callable<List<Transaction>> task = () -> this.bitcoinClient.getBlock(currentBlock).getTransactions();
            Future<List<Transaction>> future = executor.submit(task);
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException ex) {
                System.out.println("can not list transaction in block, retrying...");
                ex.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw e;
            } finally {
                future.cancel(true);
            }
        }
    }

    /**
     * Given list transaction txOutputs and an address addr, return list transaction in txOutputs belong to addr
     * @param txOutputs list transaction
     * @param addr address
     * @return list transaction in txOutputs belong to addr
     */
    public List<TransactionOutput> getTransactionOutputByAddress(List<TransactionOutput> txOutputs, Address addr) {
        List<TransactionOutput> txOutputsByAddress = new ArrayList<>();
        Address address=null;
        for (TransactionOutput txOut :
                txOutputs) {
            address=null;
            try {
                address=getAddressFromOutput(txOut);
                if (address==null)
                    continue;
                if (address.toString().equals(addr.toString())) {
                    txOutputsByAddress.add(txOut);
                }
            } catch (ScriptException ignore) {
                System.out.println(ignore);
            }
        }
        return txOutputsByAddress;
    }

    /**
     * Given list TransactionOutput and get this amount
     * @param txOutputs list TransactionOutput
     * @return this amount
     */
    public Coin getAmt(List<TransactionOutput> txOutputs) {
        Coin res = Coin.ZERO;
        for (TransactionOutput txOut :
                txOutputs) {
            try {
                res = res.add(txOut.getValue());
            } catch (Exception ignore) { }
        }
        return res;
    }

    /**
     * @param txInputs List of TransactionInput
     * @param address Address
     * @return
     */
    public List<TransactionOutput> getTransactionOutputOfInputByAddress(List<TransactionInput> txInputs, Address address){

        String[] preTx = new String[2];
        List<TransactionOutput> result=new ArrayList<>();
        try {
            for (TransactionInput txInput :
                    txInputs) {
                preTx = txInput.getOutpoint().toString().split(":");
                TransactionOutput output;
                Transaction transaction=this.bitcoinClient.getRawTransaction(Sha256Hash.wrap(preTx[0]));
                output = transaction.getOutput(Integer.parseInt(preTx[1]));
                result.add(output);
            }

            List<TransactionOutput> outputList=this.getTransactionOutputByAddress(result,address);
            System.out.println(outputList);
            return outputList;
        }catch (Exception ignore){
            System.out.println("Get transaction out of in by add wrong, more: " + ignore.toString());
            return null;
        }
    }

    /**
     *
     * @param txInputs List TransactionInput
     * @return List TransactionOutput
     */
    //new
    public List<TransactionOutput> getTransactionOutputOfInputs(List<TransactionInput> txInputs){

        String[] preTx = new String[2];
        List<TransactionOutput> result=new ArrayList<>();
        try {
            for (TransactionInput txInput :
                    txInputs) {
                preTx = txInput.getOutpoint().toString().split(":");
                TransactionOutput output;
                Transaction transaction=this.bitcoinClient.getRawTransaction(Sha256Hash.wrap(preTx[0]));
                output = transaction.getOutput(Integer.parseInt(preTx[1]));
                result.add(output);
            }
            return result;
        }catch (Exception ignore){
            System.out.println("Get transaction out of in by add wrong, more: " + ignore.toString());
            return null;
        }
    }

    public void Clustering(Map<Address,List<TransactionOutput>> wallet, List<TransactionOutput> outputs){
        Address address;
        List<TransactionOutput> valueOutput;
        for (TransactionOutput txOut :
                outputs) {
            address=this.getAddressFromOutput(txOut);
            valueOutput=wallet.get(address);
            if (valueOutput!=null){
                valueOutput.add(txOut);
            }
        }
    }

    //

    public Address newAddress(){
        try {
            Address address = this.bitcoinClient.getNewAddress();
            return address;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public ECKey getKeyofAddress(Address address){
        try {
            ECKey priKey= this.bitcoinClient.dumpPrivKey(address);
            return priKey;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Address getAddressFromOutput(TransactionOutput txOut){
        Address address=null;
        Script script=txOut.getScriptPubKey();
        if (!script.isSentToAddress() && !script.isPayToScriptHash()) {
            if (script.isSentToRawPubKey()) {
                //buf.append(" to pubkey ").append(Utils.HEX.encode(script.getPubKey()));
                address=txOut.getAddressFromP2PKHScript(this.networkParameters);
            } else if (script.isSentToMultiSig()) {
                //buf.append(" to multisig");
                System.out.println("To Multisig "+txOut.toString());
            } else {
                //System.out.println(" (unknown type)");
            }
        }else {
            address=txOut.getScriptPubKey().getToAddress(this.networkParameters);
        }
        return address;
    }

    public TransactionOutput getTxOFromUTxO(UTxOOBj uTxOOBj){
        try {
            Transaction rawTransaction = this.bitcoinClient.getRawTransaction(Sha256Hash.wrap(uTxOOBj.getTransactionId()));
            if (rawTransaction!=null)
                return rawTransaction.getOutput(uTxOOBj.getOutputIndex());
            else return null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public UTxOOBj getUTxOFromTxO(TransactionOutput txO){
        UTxOOBj result=new UTxOOBj(txO.getParentTransaction().getHash().toString(),txO.getIndex(),false);
        return result;
    }



    //-------------------------------------------------------------------------
    // NVQHuy's functions
    //-------------------------------------------------------------------------

    public static Script create2of3MultiSigRedeemScript(ECKey clientPubKey_1, ECKey clientPubKey_2, ECKey serverPubKey) {
        List<ECKey> pubkeys = new ArrayList<>();
        pubkeys.add(clientPubKey_1);
        pubkeys.add(clientPubKey_2);
        pubkeys.add(serverPubKey);
        return ScriptBuilder.createMultiSigOutputScript(2, pubkeys);
    }
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
    public static Transaction create2of3MultiSigRawTx(NetworkParameters params, List<TransactionOutput> unspentTxOutputs, Script script2of3MultiSigRedeem, List<Pair<Address,Coin>> candidates, Coin feePerKb) throws InsufficientMoneyException {
        Transaction rawTx = new Transaction(params);
        Coin maxMinNonDustValue = Coin.ZERO;
        for (Pair<Address, Coin> candidate :
                candidates) {
            TransactionOutput txOut = rawTx.addOutput(candidate.getSecond(),candidate.getFirst());
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
