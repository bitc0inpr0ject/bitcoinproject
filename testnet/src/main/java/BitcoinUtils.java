import wf.bitcoin.javabitcoindrpcclient.BitcoinJSONRPCClient;
import wf.bitcoin.javabitcoindrpcclient.BitcoindRpcClient;

import java.net.URL;
import java.util.List;
import java.util.concurrent.*;

public class BitcoinUtils {

    private static BitcoindRpcClient client = null;

    public static BitcoindRpcClient getBitcoindRpcClientInstance() {
        if (client == null) {
            String server = "192.168.1.115:18332";
            String username = "ndhy";
            String password = "12345";
            try {
                URL url = new URL("http://" + username + ":" + password + "@" + server);
                client = new BitcoinJSONRPCClient(url);
            } catch (Exception e) {
                e.printStackTrace();
                client = null;
            }
        }
        return client;
    }

    public static List<String> getTransactionInBlock(int currentBlock) throws ExecutionException {
        while (true) {
            ExecutorService executor = Executors.newCachedThreadPool();
            Callable<List<String>> task = () -> client.getBlock(currentBlock).tx();
            Future<List<String>> future = executor.submit(task);
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException ex) {
                // no log --> do nothing
            } catch (ExecutionException e) {
                throw e;
            } finally {
                future.cancel(true);
            }
        }
    }

    public static BitcoindRpcClient.RawTransaction getTransaction(String hash) throws ExecutionException {
        while (true) {
            ExecutorService executor = Executors.newCachedThreadPool();
            Callable<BitcoindRpcClient.RawTransaction> task = () -> client.getRawTransaction(hash);
            Future<BitcoindRpcClient.RawTransaction> future = executor.submit(task);
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException ex) {
                // no log --> do nothing
            } catch (ExecutionException e) {
                throw e;
            } finally {
                future.cancel(true);
            }
        }
    }

    public static int getBlockCount() throws ExecutionException {
        while (true) {
            ExecutorService executor = Executors.newCachedThreadPool();
            Callable<Integer> task = () -> client.getBlockCount();
            Future<Integer> future = executor.submit(task);
            try {
                return future.get(5, TimeUnit.SECONDS);
            } catch (TimeoutException | InterruptedException ex) {
                // no log --> do nothing
            } catch (ExecutionException e) {
                throw e;
            } finally {
                future.cancel(true);
            }
        }
    }

}
