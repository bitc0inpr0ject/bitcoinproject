package bitcoin;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import com.msgilligan.jsonrpc.JsonRPCStatusException;
import org.bitcoinj.core.*;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.RegTestParams;
import org.bitcoinj.params.TestNet2Params;
import org.bitcoinj.script.Script;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class BitcoinUtils {
    private String ServerAddress = "";
    private String RpcUsername = "";
    private String RpcPassword = "";
    private BitcoinClient bitcoinClient;
    private NetworkParameters networkParameters;

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
                if (address.toString().equals(addr.toString()))
                    txOutputsByAddress.add(txOut);
            } catch (ScriptException ignore) {
                System.out.println(ignore);
            }
        }
        return txOutputsByAddress;
    }

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
            return this.getTransactionOutputByAddress(result,address);
        }catch (Exception ignore){
            System.out.println("Get transaction out of in by add wrong, more: " + ignore.toString());
            return null;
        }
    }

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
                System.out.println(" (unknown type)");
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
        UTxOOBj result=new UTxOOBj(txO.getParentTransaction().getHash().toString(),txO.getIndex());
        return result;
    }

}
