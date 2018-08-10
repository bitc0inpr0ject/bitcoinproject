package bitcoin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("BitcoinWalletService")
public class BitcoinWalletServiceImpl implements BitcoinWalletService {

    @Autowired
    BitcoinWalletDAO bitcoinwalletDAO;
    @Override
    public void create(BitcoinWallet wallet) {
        bitcoinwalletDAO.create(wallet);
    }

    @Override
    public void update(BitcoinWallet wallet) {
        bitcoinwalletDAO.update(wallet);
    }

    @Override
    public void delete(BitcoinWallet wallet) {
        bitcoinwalletDAO.delete(wallet);
    }

    @Override
    public BitcoinWallet find(BitcoinWallet wallet) {
        return null;
    }

    @Override
    public List<BitcoinWallet> findAll() {
        return bitcoinwalletDAO.findAll();
    }
}
