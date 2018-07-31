package BitcoinModel;

import BitcoinService.BitcoinUtils;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import javafx.util.Pair;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;

import java.util.*;

public class BitcoinWallet {
    private String serverPrivKey = "";
    private String address = "";
    private String redeemScript = "";
    private long balance = 0;
    private Set<BitcoinTransactionOutput> txOutputs = new HashSet<>();

    private BitcoinWallet() { }

    public static BitcoinWallet createBitcoinWallet(ECKey clientPubKey_1, ECKey clientPubKey_2, ECKey serverPrivKey) {
        BitcoinClient bitcoinClient;
        NetworkParameters params;
        String privKey;
        Address address;
        Script redeemScript;
        BitcoinWallet bitcoinWallet = new BitcoinWallet();
        try {
            bitcoinClient = BitcoinUtils.getBitcoinClientInstance();
            params = bitcoinClient.getNetParams();
            privKey = serverPrivKey.getPrivateKeyEncoded(params).toString();
            redeemScript = BitcoinUtils.create2of3MultiSigRedeemScript(clientPubKey_1,clientPubKey_2,serverPrivKey);
            address = BitcoinUtils.create2of3MultiSigAddress(redeemScript);
            bitcoinWallet.serverPrivKey = privKey;
            bitcoinWallet.address = address.toString();
            bitcoinWallet.redeemScript = Base64.getEncoder().encodeToString(redeemScript.getProgram());
        } catch (Exception ignore) {
            return null;
        }
        return bitcoinWallet;
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

    public Transaction createRawTx(List<Pair<Address,Coin>> candidates, Coin feePerKb) throws InsufficientMoneyException {
        BitcoinClient bClient = BitcoinUtils.getBitcoinClientInstance();
        return BitcoinUtils.create2of3MultiSigRawTx(
                BitcoinTransactionOutput.getTransactionOutputList(bClient,getTxOutputs()),
                getRedeemScript(),
                candidates,
                feePerKb);
    }
    public List<Sha256Hash> RawTxHashes(Transaction rawTx) {
        return BitcoinUtils.create2of3MultiSigRawTxHash(rawTx,getRedeemScript());
    }
    public Transaction signAndSendTx(Transaction rawTx, List<TransactionSignature> userTxSign) {
        BitcoinClient bitcoinClient;
        NetworkParameters params;
        Transaction tx;
        try {
            bitcoinClient = BitcoinUtils.getBitcoinClientInstance();
            params = bitcoinClient.getNetParams();
            tx = BitcoinUtils.signRaw2of3MultiSigTransaction(
                    rawTx,
                    getRedeemScript(),
                    userTxSign,
                    DumpedPrivateKey.fromBase58(params,getServerPrivKey()).getKey());
            bitcoinClient.sendRawTransaction(tx);
        } catch (Exception ignore) {
            return null;
        }
        return tx;
    }
}
