import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.bitcoinj.core.*;
import java.util.List;

public class ConnectServer {
    public static void main(String[] args){
        if(BitcoinUtils.getBitcoinClientInstance() == null){
            System.out.println("Connect fails");
        }
        else {
            try {
                BitcoinClient bitcoinClient = BitcoinUtils.getBitcoinClientInstance();
                System.out.println(BitcoinUtils.getBlockCount());
                List<Transaction> transactionList = BitcoinUtils.getTransactionInBlock(1353060);
                Transaction transaction = transactionList.get(1);
                System.out.println(bitcoinClient.getRawTransaction(transaction.getHash()));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
