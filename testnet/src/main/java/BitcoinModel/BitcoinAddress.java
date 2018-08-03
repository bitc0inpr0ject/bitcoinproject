package BitcoinModel;

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

    public static BitcoinAddress createBitcoinAddress(NetworkParameters params, String privKey) {
        try {
            ECKey key = DumpedPrivateKey.fromBase58(params,privKey).getKey();
            BitcoinAddress bAddress = new BitcoinAddress();
            bAddress.privKey = key.getPrivateKeyEncoded(params).toString();
            bAddress.address = key.toAddress(params).toString();
            return bAddress;
        } catch (Exception ignore) {
            return null;
        }
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
        List<BitcoinTransactionOutput> list = new ArrayList<>();
        for (BitcoinTransactionOutput txOutput :
                this.txOutputs) {
            boolean chk = true;
            for (BitcoinTransactionOutput bTxOutput :
                    bTxOutputs) {
                if (txOutput.equals(bTxOutput)) {
                    chk = false;
                    break;
                }
            }
            if (chk) list.add(txOutput);
        }
        this.setTxOutputs(list);
    }

}
