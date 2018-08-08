package bitcoin;

import org.bitcoinj.core.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class HandleBlockNotify {

    /**
     * When receiving a new block, this function filters all transaction input and output,
     * with each input and ouput transaction, evaluating the address in it and finding in the Map Object
     * that contains the tracking address, if found then update this Map's value
     * @param block block index of block
     */
    //-------------------------
    public static void newcheckBlock(int block){
        List<TransactionOutput> txOutputs = new ArrayList<>();
        List<TransactionOutput> txOutputsFromInputs = new ArrayList<>();
        List<TransactionInput> txInputs = new ArrayList<>();
        List<Transaction> txs;
        Map<Address,List<TransactionOutput>> toWallet = new HashMap<Address,List<TransactionOutput>>();
        Map<Address,List<TransactionOutput>> fromWallet = new HashMap<Address,List<TransactionOutput>>();
        try {
            txs = App.bitcoinUtils.getTransactionInBlock(block);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return;
        }

        for (BTCWallet wallet :
                App.btcWallets) {
            toWallet.put(wallet.getAddress(),new ArrayList<>());
            fromWallet.put(wallet.getAddress(),new ArrayList<>());
        }

        for (Transaction tx :
                txs) {
            if (tx.isCoinBase()) {

                continue;
            }
            txOutputs.addAll(tx.getOutputs());
            txInputs.addAll(tx.getInputs());
        }

        txOutputsFromInputs.addAll(App.bitcoinUtils.getTransactionOutputOfInputs(txInputs));
        txInputs.clear();

        App.bitcoinUtils.Clustering(toWallet,txOutputs);

        App.bitcoinUtils.Clustering(fromWallet,txOutputsFromInputs);
        txOutputs.clear();
        txOutputsFromInputs.clear();

        for (BTCWallet wallet :
                App.btcWallets) {
            txOutputs=toWallet.get(wallet.getAddress());
            txOutputsFromInputs=fromWallet.get(wallet.getAddress());
            if (txOutputs.size()>0){
                App.walletService.incTxOut(wallet, txOutputs);
            }
            if (txOutputsFromInputs.size()>0){
                App.walletService.decTxOut(wallet, txOutputsFromInputs);
            }
        }
    }
    //-------------------------

    //-------------------------
    public static void checkBlockDB(int block){
        List<TransactionOutput> txOutputs = new ArrayList<>();
        List<TransactionOutput> txOutputsFromInputs = new ArrayList<>();
        List<TransactionInput> txInputs = new ArrayList<>();
        List<Transaction> txs;
        Map<Address,List<TransactionOutput>> toWallet = new HashMap<Address,List<TransactionOutput>>();
        Map<Address,List<TransactionOutput>> fromWallet = new HashMap<Address,List<TransactionOutput>>();
        try {
            txs = App.bitcoinUtils.getTransactionInBlock(block);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return;
        }

        for (BTCWallet wallet :
                App.btcWallets) {
            toWallet.put(wallet.getAddress(),new ArrayList<>());
            fromWallet.put(wallet.getAddress(),new ArrayList<>());
        }

        for (Transaction tx :
                txs) {
            if (tx.isCoinBase()) {

                continue;
            }
            txOutputs.addAll(tx.getOutputs());
            txInputs.addAll(tx.getInputs());
        }

        txOutputsFromInputs.addAll(App.bitcoinUtils.getTransactionOutputOfInputs(txInputs));
        txInputs.clear();

        App.bitcoinUtils.Clustering(toWallet,txOutputs);

        App.bitcoinUtils.Clustering(fromWallet,txOutputsFromInputs);
        txOutputs.clear();
        txOutputsFromInputs.clear();

        for (BTCWallet wallet :
                App.btcWallets) {
            txOutputs=toWallet.get(wallet.getAddress());
            txOutputsFromInputs=fromWallet.get(wallet.getAddress());
            if (txOutputs.size()>0){
                App.walletService.incTxOut(wallet, txOutputs);
            }
            if (txOutputsFromInputs.size()>0){
                App.walletService.decTxOut(wallet, txOutputsFromInputs);
            }
        }
    }
    //-------------------------

}
