import BitcoinModel.BitcoinAddress;
import BitcoinModel.BitcoinTransactionOutput;
import BitcoinService.BitcoinAddressService;
import BitcoinService.MongoDbService;
import javafx.util.Pair;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet2Params;
import org.bitcoinj.script.Script;
import org.bitcoinj.wallet.Wallet;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

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
                List<TransactionInput> transactionInputs = BitcoinUtils.getTransactionInputInBlock(1355499);
                List<TransactionOutput> transactionOutputs = BitcoinUtils.getTransactionOutputInBlock(1355499);

//                List<BitcoinTransactionOutput> bitcoinTransactionOutputList = new ArrayList<>();
//                bitcoinTransactionOutputList.add(new BitcoinTransactionOutput()
//                        .setTransactionOutput(BitcoinUtils.getTransaction("8688ad58c65b7e6ea3c24bef1304c6f62390234037e5a832bde11b218159d3d7").getOutput(1)));
//                bitcoinTransactionOutputList.add(new BitcoinTransactionOutput()
//                        .setTransactionOutput(BitcoinUtils.getTransaction("4cacf08e399e93a175ec7ed3226aa9c3270a24ae7c66455671d874059ad06f95").getOutput(1)));
//                bitcoinTransactionOutputList.add(new BitcoinTransactionOutput()
//                        .setTransactionOutput(BitcoinUtils.getTransaction("e39d264187da98af7edf55ce1f5f4456f43531e9c9e4f6da180cc4528d12be20").getOutput(0)));
//                BitcoinAddress address = new BitcoinAddress();
//                address.setAddress("mmiPYmupSstwHKkSC4qM6q4G4n5b8B9HVu");
//                address.setPrivKey("cTf7xQ6KoBQpiN2UyrYVtSXfzgkfMg32tGMgh59BLxmX5qmBjmA2");
//                address.setTxOutputs(bitcoinTransactionOutputList);
//                BitcoinAddressService.save(MongoDbService.getMongoTemplateInstance(),params,address);
//                address = BitcoinAddressService.load(MongoDbService.getMongoTemplateInstance(),address.getAddress());

                List<ECKey> pubKeys = new ArrayList<>();
                pubKeys.add(DumpedPrivateKey.fromBase58(params,"cTf7xQ6KoBQpiN2UyrYVtSXfzgkfMg32tGMgh59BLxmX5qmBjmA2").getKey());
                pubKeys.add(DumpedPrivateKey.fromBase58(params,"cUdyjQyR3VVJfB6mEAJd3E9xWEFs4pfwbm3PVhmRaTczQVaDkcUY").getKey());
                pubKeys.add(DumpedPrivateKey.fromBase58(params,"cSmtVqfTnr4xPfMMu5MEpjE65rkvsLR5mytJzGXZUFKCmwjiJKT9").getKey());
                Script script = BitcoinUtils.create2of3MultiSigRedeemScript(pubKeys);
                for (ECKey pubkey :
                        script.getPubKeys()) {
                    System.out.println(pubkey.toAddress(params));
                }

                System.out.println("Done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
