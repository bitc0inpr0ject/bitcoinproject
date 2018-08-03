package bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;

import java.util.List;
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
}
