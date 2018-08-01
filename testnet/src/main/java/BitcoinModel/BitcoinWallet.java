package BitcoinModel;

import BitcoinService.BitcoinUtils;
import org.bitcoinj.core.*;
import org.bitcoinj.script.Script;

import java.util.*;

public class BitcoinWallet {
    private String serverPrivKey = "";
    private String address = "";
    private String redeemScript = "";
    private long balance = 0;
    private Set<BitcoinTransactionOutput> txOutputs = new HashSet<>();

    private BitcoinWallet() { }

    public static BitcoinWallet createBitcoinWallet(NetworkParameters params, ECKey clientPubKey_1, ECKey clientPubKey_2, ECKey serverPrivKey) {
        try {
            String privKey = serverPrivKey.getPrivateKeyEncoded(params).toString();
            Script redeemScript = BitcoinUtils.create2of3MultiSigRedeemScript(clientPubKey_1,clientPubKey_2,serverPrivKey);
            Address address = BitcoinUtils.create2of3MultiSigAddress(params,redeemScript);
            BitcoinWallet bitcoinWallet = new BitcoinWallet();
            bitcoinWallet.serverPrivKey = privKey;
            bitcoinWallet.address = address.toString();
            bitcoinWallet.redeemScript = Base64.getEncoder().encodeToString(redeemScript.getProgram());
            return bitcoinWallet;
        } catch (Exception ignore) {
            return null;
        }
    }

    public String getServerPrivKey() {
        return serverPrivKey;
    }
    public String getAddress() {
        return address;
    }
    public Script getRedeemScript() {
        return new Script(Base64.getDecoder().decode(redeemScript));
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
