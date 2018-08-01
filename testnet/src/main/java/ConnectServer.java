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
                BitcoinUtils.getNotify(MongoDbService.getMongoTemplateInstance(),BitcoinUtils.getBitcoinClientInstance(),1);

//                BitcoinAddress address = BitcoinAddress.createBitcoinAddress(params,"cSmtVqfTnr4xPfMMu5MEpjE65rkvsLR5mytJzGXZUFKCmwjiJKT9");
//                BitcoinAddressService.save(MongoDbService.getMongoTemplateInstance(),address);
//                BitcoinWallet wallet = BitcoinWallet.createBitcoinWallet(
//                        params,
//                        ECKey.fromPublicOnly(DumpedPrivateKey.fromBase58(params,"cVJXn1fYezvJRYGphvtvsmE5tyD5WCmKE2d72bJQ7hSYwWK6rPYQ").getKey().getPubKeyPoint()),
//                        ECKey.fromPublicOnly(DumpedPrivateKey.fromBase58(params,"cUeaPvaHz1SepBcznwS3EYoMAY8tQFcGRaYmXLqQeqH2fop4RA6Y").getKey().getPubKeyPoint()),
//                        DumpedPrivateKey.fromBase58(params,"cSmtVqfTnr4xPfMMu5MEpjE65rkvsLR5mytJzGXZUFKCmwjiJKT9").getKey());
//                BitcoinWalletService.save(MongoDbService.getMongoTemplateInstance(),wallet);

//                BitcoinUtils.updateDB(
//                        MongoDbService.getMongoTemplateInstance(),
//                        BitcoinUtils.getBitcoinClientInstance(),
//                        1356461,
//                        1);

                BitcoinAddressService.sendToAddresses(
                        MongoDbService.getMongoTemplateInstance(),
                        "ms5fFtefrWVEPZeg8b3LM9ZMmYcM4NuSkh",
                        BitcoinUtils.getBitcoinClientInstance(),
                        Collections.singletonList(new Pair<>(
                                Address.fromBase58(params,"2Mtmik5182xAATbKvp9Jg1dM6KCfEGvgnfS"),
                                Coin.valueOf(1000000))),
                        Coin.parseCoin("0.001"));

                System.out.println("Done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
