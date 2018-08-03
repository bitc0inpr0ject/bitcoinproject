package bitcoin;

import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.bitcoinj.params.TestNet2Params;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class App {
    public static List<BTCWallet> btcWallets;
    public static WalletService walletService;
    public static BitcoinUtils bitcoinUtils;
    public static void main(String args[]) {

        AbstractApplicationContext context = new AnnotationConfigApplicationContext(ApplicationConfig.class);
        walletService = (WalletService) context.getBean("WalletService");
        bitcoinUtils=new BitcoinUtils();
        bitcoinUtils.Generator("testnet","http://192.168.1.38:18332","ndhy","12345");
        BitcoinWalletService bitcoinWalletService=(BitcoinWalletService) context.getBean("BitcoinWalletService");
        btcWallets=new ArrayList<>();

        //BTCWallet btcWallet;
        //btcWallet=newBTCWallet();
        //btcWallets.add(btcWallet);
        //walletService.create(btcWallet);

        //btcWallets.add();
        //btcWallets.add(newBTCWallet());

        btcWallets=walletService.findAll();
        //btcWallets.get(0)
        //showListWallet();

        Thread GetNotify = new Thread(new Runnable() {
            public void run()
            {
                (new ServerUtils()).Generator();
            }});
        GetNotify.start();
        NetworkParameters params = new TestNet2Params();
//        ECKey clientPubKey_1 = ECKey.fromPublicOnly(DumpedPrivateKey.fromBase58(params,"cVJXn1fYezvJRYGphvtvsmE5tyD5WCmKE2d72bJQ7hSYwWK6rPYQ").getKey().getPubKeyPoint());
//        ECKey clientPubKey_2 = ECKey.fromPublicOnly(DumpedPrivateKey.fromBase58(params,"cUeaPvaHz1SepBcznwS3EYoMAY8tQFcGRaYmXLqQeqH2fop4RA6Y").getKey().getPubKeyPoint());
//        ECKey serverPrivKey = DumpedPrivateKey.fromBase58(params,"cSmtVqfTnr4xPfMMu5MEpjE65rkvsLR5mytJzGXZUFKCmwjiJKT9").getKey();
//        BitcoinWallet bitcoinWallet = BitcoinWallet.createBitcoinWallet(params,clientPubKey_1,clientPubKey_2,serverPrivKey);
//        bitcoinWalletService.create(bitcoinWallet);

        List<BitcoinWallet> bitcoinWallets=bitcoinWalletService.findAll();
        System.out.println(bitcoinWallets);
        BitcoinWallet bitcoinWallet = null;
        for (BitcoinWallet wallet :
                bitcoinWallets) {
            System.out.println(wallet.getServerPrivKey(params));
            System.out.println(wallet.getAddress(params));
            System.out.println(wallet.getRedeemScript());
            BTCWallet btcWallet = new BTCWallet(wallet.getAddress(params));
            if (wallet.getAddress(params).toString().equals("2Mtmik5182xAATbKvp9Jg1dM6KCfEGvgnfS"))
                bitcoinWallet = wallet;
//            walletService.create(btcWallet);
//            btcWallets.add(btcWallet);
        }
        BitcoinClient client = bitcoinUtils.getClientInstance();

        if (bitcoinWallet != null) {
            System.out.println(bitcoinWallet.getAddress(params));
            BTCWallet btcWallet=walletService.find(bitcoinWallet.getAddress(params));
            List<TransactionOutput> transactionOutputs = btcWallet.getuTxOs();
            try {
                Transaction tx = bitcoinWallet.createRawTx(
                        params,
                        transactionOutputs,
                        Collections.singletonList(Pair.of(Address.fromBase58(params, "ms5fFtefrWVEPZeg8b3LM9ZMmYcM4NuSkh"), Coin.parseCoin("0.05"))),
                        Coin.parseCoin("0.003"));
                System.out.println(tx);
                List<Sha256Hash> hashes = bitcoinWallet.createRawTxHashes(tx);
                List<TransactionSignature> transactionSignatures = BitcoinUtils.create2of3MultiSigTxSig(
                        hashes,
                        DumpedPrivateKey.fromBase58(params,"cVJXn1fYezvJRYGphvtvsmE5tyD5WCmKE2d72bJQ7hSYwWK6rPYQ").getKey());
                List<TransactionOutput> outputs = bitcoinWallet.signAndSendTx(client,tx,transactionSignatures);
                System.out.println(tx);
                if (outputs != null) {
                    for (TransactionOutput txOut :
                            outputs) {
                        System.out.println(txOut);
                    }
                    btcWallet.removeUTxOs(outputs);
                    walletService.update(btcWallet);
                }
            } catch (InsufficientMoneyException e) {
                e.printStackTrace();
            }
        }


        context.close();
        System.out.println("Done");
    }

    public static BTCWallet newBTCWallet(){
        Address address=bitcoinUtils.newAddress();
        //ECKey key = bitcoinUtils.getKeyofAddress(address);
        return new BTCWallet(address);
    }

    public static void showListWallet(){
        for (BTCWallet wallet :
                btcWallets) {
            System.out.println("Wallet Info:");
            System.out.println("Wallet ID: " + wallet.getId().toString());
            System.out.println("Wallet Address: " + wallet.getAddress().toString());
            System.out.println("Wallet Balance: " + wallet.getBalance().toString());
            System.out.println("Wallet UTxOs: ");
            for (TransactionOutput txOut :
                    wallet.getuTxOs()) {
                System.out.println(txOut);
            }
            System.out.println("------------------------");
        }
    }
}
