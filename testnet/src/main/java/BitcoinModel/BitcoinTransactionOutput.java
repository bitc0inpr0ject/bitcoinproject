package BitcoinModel;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.TransactionOutput;

import java.util.ArrayList;
import java.util.List;

public class BitcoinTransactionOutput {
    private String address = "";
    private String txHash = "";
    private long index = -1;
    private long value = -1;

    private BitcoinTransactionOutput() { }

    public static BitcoinTransactionOutput createBitcoinTransactionOutput(BitcoinClient client, String txHash, long index) {
        NetworkParameters params;
        TransactionOutput txOutput;
        try {
            params = client.getNetParams();
            txOutput = client.getRawTransaction(Sha256Hash.wrap(txHash)).getOutput(index);
        } catch (Exception ignore) {
            return null;
        }
        BitcoinTransactionOutput bTxOutput = new BitcoinTransactionOutput();
        bTxOutput.address = txOutput.getAddressFromP2PKHScript(params).toString();
        bTxOutput.txHash = txOutput.getParentTransaction().getHashAsString();
        bTxOutput.index = txOutput.getIndex();
        bTxOutput.value = txOutput.getValue().value;
        return bTxOutput;
    }

    public String getAddress() {
        return address;
    }
    public String getTxHash() {
        return txHash;
    }
    public long getIndex() {
        return index;
    }
    public long getValue() {
        return value;
    }
    public TransactionOutput getTransactionOutput(BitcoinClient client) {
        try {
            return client.getRawTransaction(Sha256Hash.wrap(this.txHash)).getOutput(this.index);
        } catch (Exception ignore) {
            return null;
        }
    }
    public static List<TransactionOutput> getTransactionOutputList(BitcoinClient client, List<BitcoinTransactionOutput> bTxOutputs) {
        List<TransactionOutput> txOutputs = new ArrayList<>();
        for (BitcoinTransactionOutput bTxOutput :
                bTxOutputs) {
            TransactionOutput txOutput = bTxOutput.getTransactionOutput(client);
            if (txOutput == null) continue;
            txOutputs.add(txOutput);
        }
        return txOutputs;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof BitcoinTransactionOutput)) return false;
        BitcoinTransactionOutput otherBitcoinTransactionOutput = (BitcoinTransactionOutput)other;
        return this.txHash.equals(otherBitcoinTransactionOutput.txHash)
                && this.index == otherBitcoinTransactionOutput.index;
    }

}
