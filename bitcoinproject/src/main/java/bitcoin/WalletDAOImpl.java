package bitcoin;

import org.bitcoinj.core.Address;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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
        /*Query query = new Query();
        query.addCriteria(Criteria.where("address").is(wallet.getAddress()));
        Update update = new Update();
       update.addToSet("txOuts", wallet.getuTxOList());
        mongoTemplate.upsert(query, update, BTCWallet.class);*/
        mongoTemplate.save(wallet);
    }

    @Override
    public void removeUtxo(BTCWallet wallet, UTxOOBj uTxOOBj) {
        Query query = new Query();
        query.addCriteria(Criteria.where("address").is(wallet.getAddress()));
        Update update = new Update();
        update.pull("txOuts", uTxOOBj);
        mongoTemplate.upsert(query, update, BTCWallet.class);
    }


    @Override
    public void delete(BTCWallet wallet) {
        mongoTemplate.remove(wallet);
    }

    @Override
    public BTCWallet find(Address address) {
        Query query=new Query(Criteria.where("address").is(address.toBase58()));
        return mongoTemplate.findOne(query,BTCWallet.class,collectionName);
    }

    @Override
    public List<BTCWallet> findAll() {
        return mongoTemplate.findAll(BTCWallet.class);
    }
}
