package bitcoin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@Qualifier("BitcoinWalletDAO")
public class BitcoinWalletDAOImpl implements BitcoinWalletDAO {
    @Autowired
    MongoTemplate mongoTemplate;
    final String collectionName="BitcoinWalletInfo";
    @Override
    public void create(BitcoinWallet wallet) {
        mongoTemplate.insert(wallet);
    }

    @Override
    public void update(BitcoinWallet wallet) {
        mongoTemplate.save(wallet);
    }

    @Override
    public void delete(BitcoinWallet wallet) {
        mongoTemplate.remove(wallet);
    }

    @Override
    public BitcoinWallet find(BitcoinWallet wallet) {
        return null;
    }

    @Override
    public List<BitcoinWallet> findAll() {
        return mongoTemplate.findAll(BitcoinWallet.class);
    }

}
