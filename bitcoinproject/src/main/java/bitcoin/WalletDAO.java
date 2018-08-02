package bitcoin;

import java.util.List;

public interface WalletDAO {
    public void create(BTCWallet wallet);
    public void update(BTCWallet wallet);
    public void delete(BTCWallet wallet);
    public BTCWallet find(BTCWallet wallet);
    public List<BTCWallet> findAll();
}
