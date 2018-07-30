package BitcoinModel;

import org.bitcoinj.core.*;

import java.util.ArrayList;
import java.util.List;

public class BitcoinAddress {
    private String privKey = "";
    private String address = "";
    private long balance = 0;
    private List<BitcoinTransactionOutput> txOutputs = new ArrayList<>();

    public String getPrivKey() {
        return privKey;
    }
    public BitcoinAddress setPrivKey(String privKey) {
        this.privKey = privKey;
        return this;
    }
    public String getAddress() {
        return address;
    }
    public BitcoinAddress setAddress(String address) {
        this.address = address;
        return this;
    }
    public long getBalance() {
        return balance;
    }
    public BitcoinAddress setBalance(long balance) {
        this.balance = balance;
        return this;
    }
    public List<BitcoinTransactionOutput> getTxOutputs() {
        return txOutputs;
    }
    public BitcoinAddress setTxOutputs(List<BitcoinTransactionOutput> txOutputs) {
        this.txOutputs = txOutputs;
        return this;
    }

    public boolean verify(NetworkParameters params) {
        try {
            ECKey key = DumpedPrivateKey.fromBase58(params,privKey).getKey();
            if (!address.equals(key.toAddress(params).toString())) return false;
            Coin bal = Coin.ZERO;
            for (BitcoinTransactionOutput txOutput :
                    txOutputs) {
                TransactionOutput transactionOutput = txOutput.getTransactionOutput(params);
                if (!address.equals(transactionOutput.getAddressFromP2PKHScript(params).toString()))
                    return false;
                bal = bal.add(transactionOutput.getValue());
            }
            if (bal.getValue()!=balance) return false;
            return true;
        } catch (Exception ignore) { }
        return false;
    }
    public void autofix(NetworkParameters params) {
        try {
            ECKey key = DumpedPrivateKey.fromBase58(params,this.privKey).getKey();
            this.address = key.toAddress(params).toString();
            List<BitcoinTransactionOutput> bitcoinTransactionOutputList = new ArrayList<>();
            Coin bal = Coin.ZERO;
            for (BitcoinTransactionOutput txOutput :
                    this.txOutputs) {
                TransactionOutput transactionOutput = txOutput.getTransactionOutput(params);
                if (!address.equals(transactionOutput.getAddressFromP2PKHScript(params).toString()))
                    continue;
                bitcoinTransactionOutputList.add(txOutput);
                bal = bal.add(transactionOutput.getValue());
            }
            this.txOutputs = bitcoinTransactionOutputList;
            this.balance = bal.getValue();
        } catch (Exception ignore) { }
    }
}
