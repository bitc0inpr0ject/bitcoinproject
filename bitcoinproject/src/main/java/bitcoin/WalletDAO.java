package bitcoin;

import org.bitcoinj.core.Address;

import java.util.List;

public interface WalletDAO {
    public void create(BTCWallet wallet);
    public void update(BTCWallet wallet);
    public void delete(BTCWallet wallet);
    public BTCWallet find(Address address);
    public List<BTCWallet> findAll();
    public void removeUtxo(BTCWallet wallet, UTxOOBj uTxOOBj);
}
