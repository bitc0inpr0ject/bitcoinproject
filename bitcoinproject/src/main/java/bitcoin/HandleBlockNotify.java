package bitcoin;

import org.bitcoinj.core.*;

import java.util.*;
import java.util.concurrent.ExecutionException;

public class HandleBlockNotify {

    public static void checkBlock(int block){
        List<TransactionOutput> txOutputs;
        List<TransactionOutput> txOutputsFromInputs;
        List<Transaction> txs;
        Address address;
        Coin amount;
        try {
            txs = App.bitcoinUtils.getTransactionInBlock(block);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return;
        }
        for (BTCWallet wallet :
                App.btcWallets) {
            address=wallet.getAddress();
            System.out.println(address);
            for (Transaction tx :
                    txs) {
                if (tx.isCoinBase())
                    continue;
                System.out.println(tx);
                txOutputs = App.bitcoinUtils.getTransactionOutputByAddress(tx.getOutputs(), address);

                if (txOutputs.size() > 0) {
                    amount = App.bitcoinUtils.getAmt(txOutputs);
                    App.walletService.incbalance(wallet, amount.value, txOutputs);

                }

                txOutputsFromInputs = App.bitcoinUtils.getTransactionOutputOfInputByAddress(tx.getInputs(), address);
                if (txOutputsFromInputs.size() > 0) {
                    amount = App.bitcoinUtils.getAmt(txOutputsFromInputs);
                    App.walletService.decbalance(wallet, amount.value, txOutputsFromInputs);
                }
            }
        }
    }

    /**
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
