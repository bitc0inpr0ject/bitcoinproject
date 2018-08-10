package bitcoin;

import org.bitcoinj.core.Address;

import java.util.List;

public interface BitcoinWalletService {
    public void create(BitcoinWallet wallet);
    public void update(BitcoinWallet wallet);
    public void delete(BitcoinWallet wallet);
    public BitcoinWallet find(BitcoinWallet wallet);
    public List<BitcoinWallet> findAll();
}
