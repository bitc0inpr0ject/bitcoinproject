package bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.TransactionOutput;

import java.util.List;

public interface WalletService {
    public void create(BTCWallet wallet);
    public void update(BTCWallet wallet);
    public void delete(BTCWallet wallet);
    public BTCWallet find(Address address);
    public List<BTCWallet> findAll();
    public void incTxOut(BTCWallet wallet, List<TransactionOutput> outputs);
    public void decTxOut(BTCWallet wallet, List<TransactionOutput> outputs);
    //public void
}
