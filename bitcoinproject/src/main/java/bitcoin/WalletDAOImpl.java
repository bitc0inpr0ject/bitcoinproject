package bitcoin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
@Qualifier("WalletDAO")
public class WalletDAOImpl implements WalletDAO{
    @Autowired
    MongoTemplate mongoTemplate;
    final String collectionName="WalletInfo";
    @Override
    public void create(BTCWallet wallet) {
        mongoTemplate.insert(wallet);
    }

    @Override
    public void update(BTCWallet wallet) {
        mongoTemplate.save(wallet);
    }

    @Override
    public void delete(BTCWallet wallet) {
        mongoTemplate.remove(wallet);
    }

    @Override
    public BTCWallet find(BTCWallet wallet) {
        Query query=new Query(Criteria.where("_id").is(wallet.getId()));
        return mongoTemplate.findOne(query,BTCWallet.class,collectionName);
    }

    @Override
    public List<BTCWallet> findAll() {
        return mongoTemplate.findAll(BTCWallet.class);
    }
}
