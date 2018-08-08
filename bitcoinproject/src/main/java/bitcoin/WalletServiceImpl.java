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

    /**
     * @param address Address
     * @return BTCWallet Object, which is contained in DB, has Address's value is param address
     */
    @Override
    public BTCWallet find(Address address) {
        return walletDAO.find(address);

    }

    /**
     * @return All of BTCWallet Object which is contained in DB.
     */
    @Override
    public List<BTCWallet> findAll() {
        return walletDAO.findAll();
    }

    /**
     * Adding List TransactionOutput outputs to wallet TransactionOutput
     * @param wallet BTCWallet Object
     * @param outputs Containing list transaction output
     */
    @Override
    public void incTxOut(BTCWallet wallet, List<TransactionOutput> outputs) {
        wallet.setUTxOs(outputs);
        walletDAO.update(wallet);
    }

    /**
     * Removing List TransactionOutput outputs in wallet TransactionOutput
     * @param wallet BTCWallet Object
     * @param outputs Containing list transaction output
     */
    @Override
    public void decTxOut(BTCWallet wallet, List<TransactionOutput> outputs) {
        wallet.removeUTxOs(outputs);
        walletDAO.update(wallet);
    }


}
