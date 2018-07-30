package BitcoinModel;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.bitcoinj.core.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BitcoinAddress {
    private String privKey = "";
    private String address = "";
    private long balance = 0;
    private Set<BitcoinTransactionOutput> txOutputs = new HashSet<>();

    private BitcoinAddress() { }

    public static BitcoinAddress createBitcoinAddress(BitcoinClient client, String privKey) {
        ECKey key;
        try {
            key = DumpedPrivateKey.fromBase58(client.getNetParams(),privKey).getKey();
        } catch (Exception ignore) {
            return null;
        }
        BitcoinAddress bAddress = new BitcoinAddress();
        bAddress.privKey = key.getPrivateKeyEncoded(client.getNetParams()).toString();
        bAddress.address = key.toAddress(client.getNetParams()).toString();
        return bAddress;
    }

    public String getPrivKey() {
        return privKey;
    }
    public String getAddress() {
        return address;
    }
    public long getBalance() {
        return balance;
    }
    public List<BitcoinTransactionOutput> getTxOutputs() {
        return new ArrayList<>(txOutputs);
    }

    public void setTxOutputs(List<BitcoinTransactionOutput> bTxOutputs) {
        this.txOutputs = new HashSet<>();
        this.balance = 0;
        addTxOutputs(bTxOutputs);
    }
    public void addTxOutputs(List<BitcoinTransactionOutput> bTxOutputs) {
        for (BitcoinTransactionOutput bTxOutput :
                bTxOutputs) {
            if (bTxOutput == null) continue;
            if (!bTxOutput.getAddress().equals(this.address)) continue;
            this.txOutputs.add(bTxOutput);
        }
        this.balance = 0;
        for (BitcoinTransactionOutput bTxOutput :
                this.txOutputs) {
            this.balance += bTxOutput.getValue();
        }
    }
    public void removeTxOutputs(List<BitcoinTransactionOutput> bTxOutputs) {
        for (BitcoinTransactionOutput bTxOutput :
                bTxOutputs) {
            if (bTxOutput == null) continue;
            if (!bTxOutput.getAddress().equals(this.address)) continue;
            for (BitcoinTransactionOutput txOutput :
                    this.txOutputs) {
                if (bTxOutput.equals(txOutput)) this.txOutputs.remove(txOutput);
            }
        }
        this.balance = 0;
        for (BitcoinTransactionOutput bTxOutput :
                this.txOutputs) {
            this.balance += bTxOutput.getValue();
        }
    }

}