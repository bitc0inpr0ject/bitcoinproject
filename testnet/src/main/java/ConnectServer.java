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
import java.util.Scanner;

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

//                BitcoinAddressService.sendToAddresses(
//                        MongoDbService.getMongoTemplateInstance(),
//                        "ms5fFtefrWVEPZeg8b3LM9ZMmYcM4NuSkh",
//                        BitcoinUtils.getBitcoinClientInstance(),
//                        Collections.singletonList(new Pair<>(
//                                Address.fromBase58(params,"2Mtmik5182xAATbKvp9Jg1dM6KCfEGvgnfS"),
//                                Coin.valueOf(1000000))),
//                        Coin.parseCoin("0.001"));

//                Transaction tx = BitcoinWalletService.createRawTx(
//                        MongoDbService.getMongoTemplateInstance(),
//                        "2Mtmik5182xAATbKvp9Jg1dM6KCfEGvgnfS",
//                        BitcoinUtils.getBitcoinClientInstance(),
//                        Collections.singletonList(new Pair<>(
//                                Address.fromBase58(params,"ms5fFtefrWVEPZeg8b3LM9ZMmYcM4NuSkh"),
//                                Coin.valueOf(500000))),
//                        Coin.parseCoin("0.001"));
//                List<Sha256Hash> hashes = BitcoinWalletService.createRawTxHashes(
//                        MongoDbService.getMongoTemplateInstance(),
//                        "2Mtmik5182xAATbKvp9Jg1dM6KCfEGvgnfS",
//                        tx);
//                List<TransactionSignature> signatures = BitcoinUtils.create2of3MultiSigTxSig(
//                        hashes,
//                        DumpedPrivateKey.fromBase58(params,"cVJXn1fYezvJRYGphvtvsmE5tyD5WCmKE2d72bJQ7hSYwWK6rPYQ").getKey());
//                BitcoinWalletService.signAndSendTx(
//                        MongoDbService.getMongoTemplateInstance(),
//                        "2Mtmik5182xAATbKvp9Jg1dM6KCfEGvgnfS",
//                        BitcoinUtils.getBitcoinClientInstance(),
//                        tx,
//                        signatures);
                Scanner scanner = new Scanner(System.in);
                String s;
                int i;
                BitcoinAddress address;
                BitcoinWallet wallet;
                ECKey privKey;
                Address to;
                Coin amt;
                Transaction tx;
                List<Sha256Hash> hashes;
                List<TransactionSignature> transactionSignatures;
                while (true) {
                    System.out.println("Send money");
                    System.out.println("1. Address(1) or Wallet(2)");
                    try {
                        i = Integer.parseInt(scanner.nextLine());
                        switch (i) {
                            case 1:
                                System.out.println("2. Address");
                                try {
                                    s = scanner.nextLine();
                                    address = BitcoinAddressService.load(MongoDbService.getMongoTemplateInstance(),s);
                                    if (address == null) {
                                        System.out.println("Error 2.2");
                                        return;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println("Error 2.1");
                                    return;
                                }
                                System.out.println("3. Private Key");
                                try {
                                    s = scanner.nextLine();
                                    privKey = DumpedPrivateKey.fromBase58(params,s).getKey();
                                    if (!privKey.toAddress(params).toString().equals(address.getAddress())) {
                                        System.out.println("Error 3.2");
                                        return;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println("Error 3.1");
                                    return;
                                }
                                System.out.println("4. Send to");
                                try {
                                    s = scanner.nextLine();
                                    to = Address.fromBase58(params,s);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println("Error 4");
                                    return;
                                }
                                System.out.println("Balance: "+Coin.valueOf(address.getBalance()).toFriendlyString());
                                System.out.println("5. Amount");
                                try {
                                    s = scanner.nextLine();
                                    amt = Coin.parseCoin(s);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println("Error 5");
                                    return;
                                }
                                try {
                                    tx = BitcoinAddressService.sendToAddresses(
                                            MongoDbService.getMongoTemplateInstance(),
                                            address.getAddress(),
                                            BitcoinUtils.getBitcoinClientInstance(),
                                            Collections.singletonList(new Pair<>(to,amt)),
                                            Coin.parseCoin("0.003"));
                                    if (tx == null) {
                                        System.out.println("Error 6.2");
                                        return;
                                    }
                                    System.out.println(tx);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println("Error 6.1");
                                    return;
                                }
                                break;
                            case 2:
                                System.out.println("2. Address");
                                try {
                                    s = scanner.nextLine();
                                    wallet = BitcoinWalletService.load(MongoDbService.getMongoTemplateInstance(),s);
                                    if (wallet == null) {
                                        System.out.println("Error 2.2");
                                        return;
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println("Error 2.1");
                                    return;
                                }
                                System.out.println("3. Private Key");
                                try {
                                    s = scanner.nextLine();
                                    privKey = DumpedPrivateKey.fromBase58(params,s).getKey();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println("Error 3.1");
                                    return;
                                }
                                System.out.println("4. Send to");
                                try {
                                    s = scanner.nextLine();
                                    to = Address.fromBase58(params,s);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println("Error 4");
                                    return;
                                }
                                System.out.println("Balance: "+Coin.valueOf(wallet.getBalance()).toFriendlyString());
                                System.out.println("5. Amount");
                                try {
                                    s = scanner.nextLine();
                                    amt = Coin.parseCoin(s);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println("Error 5");
                                    return;
                                }
                                try {
                                    tx = BitcoinWalletService.createRawTx(
                                            MongoDbService.getMongoTemplateInstance(),
                                            wallet.getAddress(),
                                            BitcoinUtils.getBitcoinClientInstance(),
                                            Collections.singletonList(new Pair<>(to,amt)),
                                            Coin.parseCoin("0.003"));
                                    if (tx == null) {
                                        System.out.println("Error 6.2");
                                        return;
                                    } else System.out.println("Ok 6.2");
                                    hashes = BitcoinWalletService.createRawTxHashes(
                                            MongoDbService.getMongoTemplateInstance(),
                                            wallet.getAddress(),
                                            tx);
                                    if (hashes == null) {
                                        System.out.println("Error 6.3.1");
                                        return;
                                    } else if (hashes.size() < 1) {
                                        System.out.println("Error 6.3.2");
                                        return;
                                    } else System.out.println("Ok 6.3");
                                    transactionSignatures = BitcoinUtils.create2of3MultiSigTxSig(
                                            hashes,
                                            privKey);
                                    if (transactionSignatures == null) {
                                        System.out.println("Error 6.4.1");
                                        return;
                                    } else if (transactionSignatures.size() < 1) {
                                        System.out.println("Error 6.4.2");
                                        return;
                                    } else System.out.println("Ok 6.4");
                                    tx = BitcoinWalletService.signAndSendTx(
                                            MongoDbService.getMongoTemplateInstance(),
                                            wallet.getAddress(),
                                            BitcoinUtils.getBitcoinClientInstance(),
                                            tx,
                                            transactionSignatures);
                                    if (tx == null) {
                                        System.out.println("Error 6.5");
                                        return;
                                    } else System.out.println("Ok 6.5");
                                    System.out.println(tx);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    System.out.println("Error 6.1");
                                    return;
                                }
                                break;
                            default:
                                System.out.println("Error 1.2");
                                return;
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Error 1.1");
                        return;
                    }
                    System.out.println("Done");
                }

//                System.out.println("Done");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
