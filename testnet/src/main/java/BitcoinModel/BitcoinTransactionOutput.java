package BitcoinModel;

import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutput;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class BitcoinTransactionOutput {
    private String transaction = "";
    private long index = 0;

    public String getTransaction() {
        return transaction;
    }
    public BitcoinTransactionOutput setTransaction(String transaction) {
        this.transaction = transaction;
        return this;
    }
    public long getIndex() {
        return index;
    }
    public BitcoinTransactionOutput setIndex(long index) {
        this.index = index;
        return this;
    }

    public TransactionOutput getTransactionOutput(NetworkParameters params) {
        byte[] bytes = Base64.getDecoder().decode(transaction);
        Transaction tx = new Transaction(params,bytes);
        return tx.getOutput(index);
    }
    public BitcoinTransactionOutput setTransactionOutput(TransactionOutput txOutput) {
        this.transaction = Base64.getEncoder().encodeToString(txOutput.getParentTransaction().bitcoinSerialize());
        this.index = txOutput.getIndex();
        return this;
    }
    public static List<BitcoinTransactionOutput> ListTxOut2ListBTxOut(List<TransactionOutput> txOutputs) {
        List<BitcoinTransactionOutput> bitcoinTransactionOutputs = new ArrayList<>();
        for (TransactionOutput txOut :
                txOutputs) {
            bitcoinTransactionOutputs.add(new BitcoinTransactionOutput().setTransactionOutput(txOut));
        }
        return bitcoinTransactionOutputs;
    }
    public static List<TransactionOutput> ListBTxOut2ListTxOut(NetworkParameters params, List<BitcoinTransactionOutput> bTxOutputs) {
        List<TransactionOutput> transactionOutputs = new ArrayList<>();
        for (BitcoinTransactionOutput bTxOut :
                bTxOutputs) {
            transactionOutputs.add(bTxOut.getTransactionOutput(params));
        }
        return transactionOutputs;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof BitcoinTransactionOutput))return false;
        BitcoinTransactionOutput otherBitcoinTransactionOutput = (BitcoinTransactionOutput)other;
        if (this.transaction.equals(otherBitcoinTransactionOutput.transaction)
                && this.index == otherBitcoinTransactionOutput.index)
            return true;
        else return false;
    }
}
