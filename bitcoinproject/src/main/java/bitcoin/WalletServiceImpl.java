package bitcoin;

import org.bitcoinj.core.Address;
import org.bitcoinj.core.TransactionOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("WalletService")
public class WalletServiceImpl implements WalletService {

    @Autowired
    WalletDAO walletDAO;
    @Override
    public void create(BTCWallet wallet) {
        walletDAO.create(wallet);
    }

    @Override
    public void update(BTCWallet wallet) {
        walletDAO.update(wallet);
    }

    @Override
    public void delete(BTCWallet wallet) {
        walletDAO.delete(wallet);
    }

    @Override
    public BTCWallet find(Address address) {
        return walletDAO.find(address);

    }

    @Override
    public List<BTCWallet> findAll() {
        return walletDAO.findAll();
    }

    @Override
    public void incbalance(BTCWallet wallet, Long value, List<TransactionOutput> outputs) {
        wallet.incBalance(value);
        wallet.setUTxOs(outputs);
        walletDAO.update(wallet);
    }

    @Override
    public void decbalance(BTCWallet wallet, Long value, List<TransactionOutput> outputs) {
        wallet.decBalance(value);
        wallet.removeUTxOs(outputs);
        walletDAO.update(wallet);
    }

    @Override
    public void incTxOut(BTCWallet wallet, List<TransactionOutput> outputs) {
        wallet.setUTxOs(outputs);
        walletDAO.update(wallet);
    }

    @Override
    public void decTxOut(BTCWallet wallet, List<TransactionOutput> outputs) {
        wallet.removeUTxOs(outputs);
        walletDAO.update(wallet);
    }


}
