package bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.TransactionOutput;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;

import java.util.ArrayList;
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

        btcWallets=new ArrayList<>();

        //BTCWallet btcWallet;
        //btcWallet=newBTCWallet();
        //btcWallets.add(btcWallet);
        //walletService.create(btcWallet);

        //btcWallets.add();
        //btcWallets.add(newBTCWallet());

        btcWallets=walletService.findAll();
        showListWallet();

        Thread GetNotify = new Thread(new Runnable() {
            public void run()
            {
                (new ServerUtils()).Generator();
            }});
        GetNotify.start();



        context.close();
        System.out.println("Done");
    }

    public static BTCWallet newBTCWallet(){
        Address address=bitcoinUtils.newAddress();
        ECKey key = bitcoinUtils.getKeyofAddress(address);
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
