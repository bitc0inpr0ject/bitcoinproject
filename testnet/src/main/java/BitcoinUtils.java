import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet2Params;

import java.net.URI;
import java.util.List;
import java.util.concurrent.*;

public class BitcoinUtils {

    private static BitcoinClient bitcoinClient = null;

    public static BitcoinClient getBitcoinClientInstance() {
        if (bitcoinClient == null) {
            String server = "http://192.168.1.115:18332";
            String username = "ndhy";
            String password = "12345";
            int dev = 1; // dev = 1 --> testnet
            try {
                NetworkParameters network;
                URI uri;
                if (dev == 0) {
                    network = new MainNetParams();
                    uri = new URI(server);

                } else {
                    network = new TestNet2Params();
                    uri = new URI(server);
                }
                System.out.println("bitcoinclient networkID" + network.getId());
                System.out.println("uri server bitcoin: " + uri.toString());
                bitcoinClient = new BitcoinClient(network, uri, username, password);
                return bitcoinClient;
            } catch (Exception e) {
                System.out.println("Can not connect to server and start bitcoinClient");
                e.printStackTrace();
                bitcoinClient = null;
            }
        }
        return bitcoinClient;
    }

    public static List<Transaction>  getTransactionInBlock(int currentBlock) throws ExecutionException {
        while (true) {
            ExecutorService executor = Executors.newCachedThreadPool();
            Callable<List<Transaction>> task = () -> bitcoinClient.getBlock(currentBlock).getTransactions();
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

    public static Transaction getTransaction(Sha256Hash hash) throws ExecutionException {
        while (true) {
            ExecutorService executor = Executors.newCachedThreadPool();
            Callable<Transaction> task = () -> getBitcoinClientInstance().getRawTransaction(hash);
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
                future.cancel(true); // may or may not desire this
            }
        }
    }

    public static int getBlockCount() throws ExecutionException {
        while (true) {
            ExecutorService executor = Executors.newCachedThreadPool();
            Callable<Integer> task = () -> bitcoinClient.getBlockCount();
            Future<Integer> future = executor.submit(task);
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException ex) {
                System.out.println("can not get Block Count detail, retrying...");
                ex.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw e;
            } finally {
                future.cancel(true);
            }
        }
    }

    public static Transaction completeTx(NetworkParameters params, Map<Pair<Transaction,Integer>, ECKey> originalInputs, List<Pair<Address, Coin>> candidates, Address changeAddress, Coin feePerKb) throws Exception {
        Transaction rawTx = new Transaction(params);
        int size = rawTx.getMessageSize();

        size += VarInt.sizeOf(candidates.size());
        for (Pair<Address, Coin> candidate:
             candidates) {
            TransactionOutput txout = rawTx.addOutput(candidate.getValue(), candidate.getKey());
            size += txout.getMessageSize();
        }

        size += VarInt.sizeOf(originalInputs.size());
        for (Pair<Transaction,Integer> originalInput:
             originalInputs.keySet()) {
            TransactionInput txinp = rawTx.addInput(new TransactionOutPoint(params, originalInput.getValue(), originalInput.getKey()).getConnectedOutput());
            size += txinp.getMessageSize();
        }
        for (int i = 0; i < rawTx.getInputs().size(); i++) {
            TransactionInput txinp = rawTx.getInput(i);
            Pair<Transaction,Integer> inp = new Pair<>(txinp.getConnectedOutput().getParentTransaction(), txinp.getConnectedOutput().getIndex());
            Script scriptSig = ScriptBuilder.createInputScript(rawTx.calculateSignature(i, originalInputs.get(inp), txinp.getConnectedOutput().getScriptPubKey(), Transaction.SigHash.ALL, false), originalInputs.get(inp));
            size += scriptSig.getProgram().length;
        }

        if (rawTx.getInputSum().isLessThan(rawTx.getOutputSum().add(feePerKb.multiply(size).divide(1000L))))
            throw new Exception("Not enough input amount ...");

        TransactionOutput changeOutput = new TransactionOutput(rawTx.getParams(), rawTx, Coin.ZERO, changeAddress);
        int changeSize = changeOutput.getMessageSize() + VarInt.sizeOf(candidates.size()+1) - VarInt.sizeOf(candidates.size());

        if (rawTx.getInputSum().isGreaterThan(rawTx.getOutputSum()
                .add(feePerKb.multiply(size).divide(1000L))
                .add(changeOutput.getMinNonDustValue(feePerKb)))) {
            size += changeSize;
            changeOutput.setValue(rawTx.getInputSum().subtract(rawTx.getOutputSum()).subtract(feePerKb.multiply(size).divide(1000L)));
            rawTx.addOutput(changeOutput);
        }

        for (int i = 0; i < rawTx.getInputs().size(); i++) {
            TransactionInput txinp = rawTx.getInput(i);
            Pair<Transaction,Integer> inp = new Pair<>(txinp.getConnectedOutput().getParentTransaction(), txinp.getConnectedOutput().getIndex());
            Script scriptSig = ScriptBuilder.createInputScript(rawTx.calculateSignature(i, originalInputs.get(inp), txinp.getConnectedOutput().getScriptPubKey(), Transaction.SigHash.ALL, false), originalInputs.get(inp));
            txinp.setScriptSig(scriptSig);
        }

        return rawTx;
    }
    
}
