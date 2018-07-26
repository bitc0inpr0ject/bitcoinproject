import javafx.util.Pair;
import org.bitcoinj.core.*;
import org.bitcoinj.params.TestNet2Params;
import org.bitcoinj.wallet.Wallet;

import java.util.*;

public class ConnectServer {
    public static void main(String[] args){
        NetworkParameters params = new TestNet2Params();
        String server = "http://192.168.1.38:18332";
        String username = "ndhy";
        String password = "12345";

        BitcoinUtils.createBitcoinClientInstance(params, server, username, password);
        if(BitcoinUtils.getBitcoinClientInstance() == null){
            System.out.println("Connect fails");
        }
        else {
            try {
                System.out.println(BitcoinUtils.getBlockCount());
                List<TransactionInput> transactionInputs = BitcoinUtils.getTransactionInputInBlock(1355499);
                List<TransactionOutput> transactionOutputs = BitcoinUtils.getTransactionOutputInBlock(1355499);
                List<TransactionOutput> tmp = BitcoinUtils.getTransactionOutputByAddress(transactionOutputs,Address.fromBase58(params,"ms5fFtefrWVEPZeg8b3LM9ZMmYcM4NuSkh"));
                for (TransactionOutput txout :
                        tmp) {
                    System.out.println(txout.toString());
                }
                

                Map<TransactionOutput,ECKey> originalInputs = new HashMap<>();
                originalInputs.put(BitcoinUtils.getTransaction("8688ad58c65b7e6ea3c24bef1304c6f62390234037e5a832bde11b218159d3d7").getOutput(1),DumpedPrivateKey.fromBase58(params,"cTf7xQ6KoBQpiN2UyrYVtSXfzgkfMg32tGMgh59BLxmX5qmBjmA2").getKey());
                originalInputs.put(BitcoinUtils.getTransaction("4cacf08e399e93a175ec7ed3226aa9c3270a24ae7c66455671d874059ad06f95").getOutput(1),DumpedPrivateKey.fromBase58(params,"cTf7xQ6KoBQpiN2UyrYVtSXfzgkfMg32tGMgh59BLxmX5qmBjmA2").getKey());
                originalInputs.put(BitcoinUtils.getTransaction("e39d264187da98af7edf55ce1f5f4456f43531e9c9e4f6da180cc4528d12be20").getOutput(0),DumpedPrivateKey.fromBase58(params,"cUdyjQyR3VVJfB6mEAJd3E9xWEFs4pfwbm3PVhmRaTczQVaDkcUY").getKey());
                List<Pair<Address,Coin>> candidates = new ArrayList<>();
                candidates.add(new Pair<>(Address.fromBase58(params,"ms5fFtefrWVEPZeg8b3LM9ZMmYcM4NuSkh"),Coin.parseCoin("0.001")));
                candidates.add(new Pair<>(Address.fromBase58(params,"mtDR8KZzniGicDaHh4V3zBnD3GVd2zkJ9x"),Coin.parseCoin("0.002")));
                System.out.println(BitcoinUtils.sendTx(originalInputs,candidates,Address.fromBase58(params,"mwpJYM6amVtCeUjGz2NYUXY3rfrQJmPJN1"),Coin.valueOf(1000)));

                System.out.println(Coin.parseCoin("0.000001"));
                System.out.println(Coin.parseCoin("0.00000554"));

                List<TransactionOutput> utxo = new ArrayList<>();
                utxo.add(BitcoinUtils.getTransaction("8688ad58c65b7e6ea3c24bef1304c6f62390234037e5a832bde11b218159d3d7").getOutput(1));
                utxo.add(BitcoinUtils.getTransaction("4cacf08e399e93a175ec7ed3226aa9c3270a24ae7c66455671d874059ad06f95").getOutput(1));
                utxo.add(BitcoinUtils.getTransaction("e39d264187da98af7edf55ce1f5f4456f43531e9c9e4f6da180cc4528d12be20").getOutput(0));
                System.out.println(BitcoinUtils.sendToAddressesByPrivKey(utxo,DumpedPrivateKey.fromBase58(params,"cTf7xQ6KoBQpiN2UyrYVtSXfzgkfMg32tGMgh59BLxmX5qmBjmA2").getKey(),candidates,Coin.valueOf(1000)));
                System.out.println("Done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
