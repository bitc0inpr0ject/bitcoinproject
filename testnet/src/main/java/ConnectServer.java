import BitcoinModel.BitcoinAddress;
import BitcoinModel.BitcoinTransactionOutput;
import BitcoinModel.BitcoinWallet;
import BitcoinService.BitcoinAddressService;
import BitcoinService.BitcoinUtils;
import BitcoinService.MongoDbService;
import javafx.util.Pair;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.TestNet2Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.script.ScriptChunk;
import org.bitcoinj.wallet.Wallet;
import org.omg.IOP.TransactionService;

import java.util.*;

public class ConnectServer {
    public static void main(String[] args){
        NetworkParameters params = new TestNet2Params();
        String server = "http://192.168.1.38:18332";
        String username = "ndhy";
        String password = "12345";

        BitcoinUtils.createBitcoinClientInstance(params, server, username, password);

        String host_db = "127.0.0.1";
        int port_db = 27017;
        String username_db = "bitcointeam";
        String password_db = "b1tc01n@";
        String dbname_db = "bitcoin_management";

        MongoDbService.createMongoTemplateInstance(host_db,port_db,username_db,password_db,dbname_db);

        if(BitcoinUtils.getBitcoinClientInstance() == null || MongoDbService.getMongoTemplateInstance() == null){
            System.out.println("Connect fails");
        }
        else {
            try {
                System.out.println(BitcoinUtils.getBlockCount());

//                BitcoinAddress address;
//                address = BitcoinAddress.createBitcoinAddress(BitcoinUtils.getBitcoinClientInstance(),
//                        "cUdyjQyR3VVJfB6mEAJd3E9xWEFs4pfwbm3PVhmRaTczQVaDkcUY");
//                BitcoinAddressService.save(MongoDbService.getMongoTemplateInstance(),address);

//                BitcoinAddressService.update(MongoDbService.getMongoTemplateInstance(),1353694);
//                BitcoinAddressService.update(MongoDbService.getMongoTemplateInstance(),1353890);
//                BitcoinAddressService.update(MongoDbService.getMongoTemplateInstance(),1355103);

//                for (ECKey key :
//                        BitcoinUtils.getTransaction("e39d264187da98af7edf55ce1f5f4456f43531e9c9e4f6da180cc4528d12be20").getInput(0).getScriptSig().getPubKeys()) {
//                    System.out.println(key.toAddress(params));
//                }

                BitcoinWallet bitcoinWallet = BitcoinWallet.createBitcoinWallet(
                        DumpedPrivateKey.fromBase58(params,"cVJXn1fYezvJRYGphvtvsmE5tyD5WCmKE2d72bJQ7hSYwWK6rPYQ").getKey(),
                        DumpedPrivateKey.fromBase58(params,"cUeaPvaHz1SepBcznwS3EYoMAY8tQFcGRaYmXLqQeqH2fop4RA6Y").getKey(),
                        DumpedPrivateKey.fromBase58(params,"cSmtVqfTnr4xPfMMu5MEpjE65rkvsLR5mytJzGXZUFKCmwjiJKT9").getKey()
                );

                Script redeemScript = BitcoinUtils.create2of3MultiSigRedeemScript(
                        DumpedPrivateKey.fromBase58(params,"cVJXn1fYezvJRYGphvtvsmE5tyD5WCmKE2d72bJQ7hSYwWK6rPYQ").getKey(),
                        DumpedPrivateKey.fromBase58(params,"cUeaPvaHz1SepBcznwS3EYoMAY8tQFcGRaYmXLqQeqH2fop4RA6Y").getKey(),
                        DumpedPrivateKey.fromBase58(params,"cSmtVqfTnr4xPfMMu5MEpjE65rkvsLR5mytJzGXZUFKCmwjiJKT9").getKey()
                        );
//                List<ECKey> keys = redeemScript.getPubKeys();
//                Collections.sort(keys,ECKey.PUBKEY_COMPARATOR);
//                for (ECKey key :
//                        keys) {
//                    System.out.println(key.toAddress(params));
//                }
                Address address = BitcoinUtils.create2of3MultiSigAddress(redeemScript);
                System.out.println(address);
                Transaction tx = BitcoinUtils.create2of3MultiSigRawTx(
                        Collections.singletonList(BitcoinUtils.getTransaction("4cacf08e399e93a175ec7ed3226aa9c3270a24ae7c66455671d874059ad06f95")
                                .getOutput(0)),
                        redeemScript,
                        Collections.singletonList(new Pair<Address,Coin>(
                                Address.fromBase58(params,"mmiPYmupSstwHKkSC4qM6q4G4n5b8B9HVu"),
                                Coin.parseCoin("0.001"))),
                        Coin.valueOf(1000));
                List<Sha256Hash> txHashes = BitcoinUtils.create2of3MultiSigRawTxHash(tx,redeemScript);
                List<TransactionSignature> txSigs = BitcoinUtils.create2of3MultiSigTxSig(txHashes,
                        DumpedPrivateKey.fromBase58(params,"cVJXn1fYezvJRYGphvtvsmE5tyD5WCmKE2d72bJQ7hSYwWK6rPYQ").getKey());
                tx = BitcoinUtils.signRaw2of3MultiSigTransaction(
                        tx,
                        redeemScript,
                        txSigs,
                        DumpedPrivateKey.fromBase58(params,"cSmtVqfTnr4xPfMMu5MEpjE65rkvsLR5mytJzGXZUFKCmwjiJKT9").getKey());
                for (TransactionInput txInput :
                        tx.getInputs()) {
                    txInput.verify();
                }
                System.out.println(tx);
                
                System.out.println("Done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
