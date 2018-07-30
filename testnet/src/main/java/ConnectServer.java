import BitcoinModel.BitcoinAddress;
import BitcoinModel.BitcoinTransactionOutput;
import BitcoinService.BitcoinAddressService;
import BitcoinService.BitcoinUtils;
import BitcoinService.MongoDbService;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet2Params;
import org.bitcoinj.script.Script;

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

                BitcoinAddress address;
                address = BitcoinAddress.createBitcoinAddress(BitcoinUtils.getBitcoinClientInstance(),
                        "cUdyjQyR3VVJfB6mEAJd3E9xWEFs4pfwbm3PVhmRaTczQVaDkcUY");
                BitcoinAddressService.save(MongoDbService.getMongoTemplateInstance(),address);

                BitcoinAddressService.update(MongoDbService.getMongoTemplateInstance(),1353694);
                BitcoinAddressService.update(MongoDbService.getMongoTemplateInstance(),1353890);
                BitcoinAddressService.update(MongoDbService.getMongoTemplateInstance(),1355103);

                System.out.println("Done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
