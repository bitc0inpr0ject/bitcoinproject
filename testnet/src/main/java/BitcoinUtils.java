import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import javafx.util.Pair;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

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

    public static List<Transaction> getTransactionInBlock(int currentBlock) throws ExecutionException {
        while (true) {
            ExecutorService executor = Executors.newCachedThreadPool();
            Callable<List<Transaction>> task = () -> bitcoinClient.getBlock(currentBlock).getTransactions();
            Future<List<Transaction>> future = executor.submit(task);
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException ex) {
                System.out.println("cannot list transaction in block, retrying...");
                ex.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw e;
            } finally {
                future.cancel(true);
            }
        }
    }

    public static Transaction getTransaction(String sha256Hash) throws ExecutionException {
        while (true) {
            ExecutorService executor = Executors.newCachedThreadPool();
            Callable<Transaction> task = () -> getBitcoinClientInstance().getRawTransaction(Sha256Hash.wrap(sha256Hash));
            Future<Transaction> future = executor.submit(task);
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException ex) {
                System.out.println("cannot get transaction detail, retrying...");
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
                System.out.println("cannot get block count detail, retrying...");
                ex.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
                throw e;
            } finally {
                future.cancel(true);
            }
        }
    }

    public static List<TransactionInput> getTransactionInputInBlock(int currentBlock) throws ExecutionException {
        List<TransactionInput> transactionInputs = new ArrayList<>();
        List<Transaction> transactions = getTransactionInBlock(currentBlock);
        for (Transaction tx :
                transactions) {
            transactionInputs.addAll(tx.getInputs());
        }
        return transactionInputs;
    }

    public static List<TransactionOutput> getTransactionOutputInBlock(int currentBlock) throws ExecutionException {
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

//        bitcoinClient.sendRawTransaction(rawTx);
        return rawTx;
    }

    public static Transaction sendToAddressesByPrivKey(List<TransactionOutput> unspentTxOutputs, ECKey privKey, List<Pair<Address,Coin>> candidates, Coin feePerKb) throws InsufficientMoneyException, IOException {
        Map<TransactionOutput,ECKey> originalInputs = new HashMap<>();
        Coin insufficientMoney = Coin.ZERO;
        for (TransactionOutput utxo :
                unspentTxOutputs) {
            if(utxo.getAddressFromP2PKHScript(bitcoinClient.getNetParams()) == null)
                continue;
            if (!utxo.getAddressFromP2PKHScript(bitcoinClient.getNetParams()).toString().equals(privKey.toAddress(bitcoinClient.getNetParams()).toString()))
                continue;
            originalInputs.put(utxo,privKey);
            try {
                return sendTx(originalInputs,candidates,privKey.toAddress(bitcoinClient.getNetParams()),feePerKb);
            } catch (InsufficientMoneyException e) {
                insufficientMoney = e.missing;
                continue;
            } catch (IOException e) {
                throw e;
            }
        }
        throw new InsufficientMoneyException(insufficientMoney);
    }

}
