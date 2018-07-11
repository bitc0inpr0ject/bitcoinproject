import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.TestNet2Params;
import java.net.URI;
import java.net.URISyntaxException;

public class ConnectServer {
    public static BitcoinClient bitcoinClient;

    public static BitcoinClient getClientInstance(){
        if(bitcoinClient == null){
            String server = "http://127.0.0.1:18332";
            String username = "ndhy";
            String password = "12345";
            try{
                NetworkParameters network;
                URI uri;
                network = new TestNet2Params();
                uri = new URI(server);
                System.out.println("bitcoinclient networkID: " + network.getId());
                System.out.println("uri server bitcoin: "+ uri.toString());
                bitcoinClient = new BitcoinClient(network,uri,username,password);
                return bitcoinClient;
            } catch (URISyntaxException e) {
                e.printStackTrace();
                bitcoinClient = null;
            }
        }

        return bitcoinClient;
    }
    public static void main(String[] args){
        bitcoinClient = getClientInstance();
        if(bitcoinClient == null){
            System.out.println("Connect fails");
        }
        else System.out.println("Connect successful");
    }
}
