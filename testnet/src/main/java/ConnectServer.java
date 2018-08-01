import BitcoinModel.BitcoinAddress;
import BitcoinModel.BitcoinWallet;
import BitcoinService.BitcoinAddressService;
import BitcoinService.BitcoinUtils;
import BitcoinService.BitcoinWalletService;
import BitcoinService.MongoDbService;
import javafx.util.Pair;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.TestNet2Params;

import java.util.Collections;
import java.util.List;

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
                System.out.println(BitcoinUtils.getBlockCount(BitcoinUtils.getBitcoinClientInstance()));

//                BitcoinAddress address = BitcoinAddress.createBitcoinAddress(BitcoinUtils.getBitcoinClientInstance(),
//                        "cSmtVqfTnr4xPfMMu5MEpjE65rkvsLR5mytJzGXZUFKCmwjiJKT9");
//                BitcoinAddressService.save(MongoDbService.getMongoTemplateInstance(),address);
//
//                BitcoinWallet wallet = BitcoinWallet.createBitcoinWallet(
//                        DumpedPrivateKey.fromBase58(params,"cVJXn1fYezvJRYGphvtvsmE5tyD5WCmKE2d72bJQ7hSYwWK6rPYQ").getKey(),
//                        DumpedPrivateKey.fromBase58(params,"cUeaPvaHz1SepBcznwS3EYoMAY8tQFcGRaYmXLqQeqH2fop4RA6Y").getKey(),
//                        DumpedPrivateKey.fromBase58(params,"cSmtVqfTnr4xPfMMu5MEpjE65rkvsLR5mytJzGXZUFKCmwjiJKT9").getKey()
//                );
//                BitcoinWalletService.save(MongoDbService.getMongoTemplateInstance(),wallet);

//                BitcoinAddressService.update(MongoDbService.getMongoTemplateInstance(),1353694);
//                BitcoinAddressService.update(MongoDbService.getMongoTemplateInstance(),1353890);
//                BitcoinAddressService.update(MongoDbService.getMongoTemplateInstance(),1355103);
//                BitcoinAddressService.update(MongoDbService.getMongoTemplateInstance(),1356311);

//                BitcoinWalletService.update(MongoDbService.getMongoTemplateInstance(),1355073);
//                BitcoinWalletService.update(MongoDbService.getMongoTemplateInstance(),1355103);

//                BitcoinAddress address = BitcoinAddressService.load(MongoDbService.getMongoTemplateInstance(),"ms5fFtefrWVEPZeg8b3LM9ZMmYcM4NuSkh");
//                BitcoinWallet wallet = BitcoinWalletService.load(MongoDbService.getMongoTemplateInstance(),"2Mtmik5182xAATbKvp9Jg1dM6KCfEGvgnfS");
//
//                Transaction tx = wallet.createRawTx(
//                        Collections.singletonList(new Pair<>(Address.fromBase58(params,address.getAddress()),Coin.valueOf(10000))),
//                        Coin.parseCoin("0.0008"));
//                List<Sha256Hash> hashes = wallet.createRawTxHashes(tx);
//                List<TransactionSignature> transactionSignatures = BitcoinUtils.create2of3MultiSigTxSig(
//                        hashes,
//                        DumpedPrivateKey.fromBase58(params,"cUeaPvaHz1SepBcznwS3EYoMAY8tQFcGRaYmXLqQeqH2fop4RA6Y").getKey());
//                System.out.println(wallet.signAndSendTx(tx,transactionSignatures));

//                BitcoinUtils.getBitcoinClientInstance().waitForBlock(1356372,3600);
//                BitcoinAddressService.update(MongoDbService.getMongoTemplateInstance(),1356372);
//                BitcoinWalletService.update(MongoDbService.getMongoTemplateInstance(),1356372);


                System.out.println("Done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
