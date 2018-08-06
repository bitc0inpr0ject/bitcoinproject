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

    /**
     * Create the bitcoin wallet.
     * The only method to create the bitcoin wallet.
     * This is the 2-of-3 multisig.
     * To export public key from ECKey, we can use method: ECKey.getPubKey(): byte[]
     * To import public key, we can use function: ECKey.fromPublicOnly(byte[] pub): ECKey
     * To export private key from ECKey, we can use method:
     *      ECKey.getPrivateKeyEncoded(NetworkParameters params).toBase58(): String
     *      with:   NetworkParameters params: the network parameter (MainNet, TestNet, ...)
     * To import private key, we can use function:
     *      DumpedPrivateKey.fromBase58(NetworkParameters params, String base58): ECKey
     *      with:   NetworkParameters params: the network parameter (MainNet, TestNet, ...)
     *              String base58: the private key in String format
     * IMPORTANT:
     * The bitcoin wallet created from (A,B,C) is different from the one created from (B,A,C).
     * So remember the order of the keys.
     * @param params            the network parameter (MainNet, TestNet, ...)
     * @param clientPubKey_1    the first client public key
     * @param clientPubKey_2    the second client public key
     * @param serverPrivKey     the server private key
     * @return
     */
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

    /**
     * Get the server private key of the bitcoin wallet
     * @return  the server private key
     */
    public String getServerPrivKey() {
        return serverPrivKey;
    }

    /**
     * Get the address of the bitcoin wallet
     * @return  the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Get the redeem script of the bitcoin wallet
     * @return  the redeem script
     */
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
