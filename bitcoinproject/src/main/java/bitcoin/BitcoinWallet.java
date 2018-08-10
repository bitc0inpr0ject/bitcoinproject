package bitcoin;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.script.Script;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Document(collection = "BitcoinWalletInfo")
public class BitcoinWallet {
    private String serverPrivKey = "1";
    private String address = "2";
    private String redeemScript = "3";

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
     * @return                  the bitcoin wallet object
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
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get the server private key of the bitcoin wallet
     * @param params            the network parameter (MainNet, TestNet, ...)
     * @return                  the server private key
     */
    public ECKey getServerPrivKey(NetworkParameters params) {
        return DumpedPrivateKey.fromBase58(params,serverPrivKey).getKey();
    }

    /**
     * Get the address of the bitcoin wallet
     * @param params            the network parameter (MainNet, TestNet, ...)
     * @return                  the address
     */
    public Address getAddress(NetworkParameters params) {
        return Address.fromBase58(params,address);
    }

    /**
     * Get the redeem script of the bitcoin wallet
     * @return                  the redeem script
     */
    public Script getRedeemScript() {
        return new Script(Base64.getDecoder().decode(redeemScript));
    }

    /**
     * Return the raw transaction which has transaction outputs and unsigned transaction inputs
     * This is the 2-of-3 multisig.
     * Use at the server side.
     * IMPORTANT:
     * 1 KB = 1000 bytes
     * RECOMMENDED:
     * feePerKb = Coin.valueOf(1000)        1000 satoshis/KB
     * @param params                        the network parameter (MainNet, TestNet, ...)
     * @param UTxOs                         the list of unspent transaction outputs
     * @param candidates                    the list of <Address,Coin> pairs which Coin is the amount we want to send to Address
     * @param feePerKb                      the price of transaction (fee per KB)
     * @return                              the raw transaction
     * @throws InsufficientMoneyException   when the input sum is not enough to send, it will throw this exception
     */
    public Transaction createRawTx(NetworkParameters params, List<TransactionOutput> UTxOs, List<Pair<Address,Coin>> candidates, Coin feePerKb) throws InsufficientMoneyException {
        try {
            Transaction tx = BitcoinUtils.create2of3MultiSigRawTx(
                    params,
                    UTxOs,
                    this.getRedeemScript(),
                    candidates,
                    feePerKb);
            return tx;
        } catch (InsufficientMoneyException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Return the list of transaction hashes.
     * This is the 2-of-3 multisig.
     * Use at the server side.
     * To be able to send the transaction, the client MUST sign them.
     * @param rawTx                     the raw transaction after created by the create2of3MultiSigRawTx function.
     * @return                          the list of transaction hashes
     */
    public List<Sha256Hash> createRawTxHashes(Transaction rawTx) {
        try {
            return BitcoinUtils.create2of3MultiSigRawTxHash(rawTx,this.getRedeemScript());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Complete and send the transaction (if possible).
     * This is the 2-of-3 multisig.
     * Use at the server side.
     * After receiving the list of transaction signatures, the server will add them, complete and send the transaction.
     * @param client                    the bitcoin client object
     * @param rawTx                     the raw transaction
     * @param userTxSign                the list of transaction signatures
     * @return                          the completed transaction
     */
    public List<TransactionOutput> signAndSendTx(BitcoinClient client, Transaction rawTx, List<TransactionSignature> userTxSign) {
        try {
            NetworkParameters params = client.getNetParams();
            rawTx = BitcoinUtils.signRaw2of3MultiSigTransaction(
                    rawTx,
                    this.getRedeemScript(),
                    userTxSign,
                    this.getServerPrivKey(params));
            client.sendRawTransaction(rawTx);
            List<TransactionOutput> txOutputs = new ArrayList<>();
            for (TransactionInput txInput :
                    rawTx.getInputs()) {
                if (txInput.getConnectedOutput() == null)
                    throw new NullPointerException("cannot get spending transaction outputs ...");
                txOutputs.add(txInput.getConnectedOutput());
            }
            return txOutputs;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
