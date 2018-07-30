package BitcoinService;

import BitcoinModel.BitcoinAddress;
import BitcoinModel.BitcoinTransactionOutput;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.ArrayList;
import java.util.List;

public class BitcoinAddressService {
    public static BitcoinAddress load(MongoTemplate db, String address) {
        return db.findOne(Query.query(Criteria.where("address").is(address)), BitcoinAddress.class);
    }

    public static void save(MongoTemplate db, NetworkParameters params, BitcoinAddress address) {
        address.autofix(params);
        db.findAndRemove(Query.query(Criteria.where("address").is(address)), BitcoinAddress.class);
        db.save(address);
    }

    public static void update(MongoTemplate db, NetworkParameters params, List<Transaction> txs) {
        for (Transaction tx :
                txs) {
            for (TransactionInput txInp :
                    tx.getInputs()) {
                BitcoinTransactionOutput bitcoinTransactionOutput = new BitcoinTransactionOutput();
                bitcoinTransactionOutput.setTransactionOutput(txInp.getConnectedOutput());
                BitcoinAddress bitcoinAddress = db.findOne(Query.query(Criteria.where("txOutputs").in(bitcoinTransactionOutput)), BitcoinAddress.class);
                bitcoinAddress.getTxOutputs().remove(bitcoinTransactionOutput);
                save(db,params,bitcoinAddress);
            }
            for (TransactionOutput txOut :
                    tx.getOutputs()) {
                String addr = txOut.getAddressFromP2PKHScript(params).toString();
                BitcoinAddress bitcoinAddress = db.findOne(Query.query(Criteria.where("address").is(addr)), BitcoinAddress.class);
                bitcoinAddress.getTxOutputs().add(new BitcoinTransactionOutput().setTransactionOutput(txOut));
                save(db,params,bitcoinAddress);
            }
        }
    }
}
