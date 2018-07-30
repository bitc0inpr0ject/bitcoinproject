package BitcoinService;

import BitcoinModel.BitcoinAddress;
import BitcoinModel.BitcoinTransactionOutput;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionInput;
import org.bitcoinj.core.TransactionOutput;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;
import java.util.Collections;

public class BitcoinAddressService {
    public static BitcoinAddress load(MongoTemplate db, String address) {
        return db.findOne(Query.query(Criteria.where("address").is(address)), BitcoinAddress.class);
    }

    public static void save(MongoTemplate db, BitcoinAddress address) {
        db.findAndRemove(Query.query(Criteria.where("address").is(address.getAddress())), BitcoinAddress.class);
        db.save(address);
    }

    public static void update(MongoTemplate db, int currentBlock) throws IOException {
        BitcoinClient bClient = BitcoinUtils.getBitcoinClientInstance();
        NetworkParameters params = bClient.getNetParams();
        for (Transaction tx :
                BitcoinUtils.getTransactionInBlock(currentBlock)) {
            for (TransactionInput txInput :
                    tx.getInputs()) {
                try {
                    if (db.findOne(Query.query(Criteria.where("txOutputs").elemMatch(
                                Criteria.where("txHash").is(txInput.getOutpoint().getHash().toString())
                                        .and("index").is(txInput.getOutpoint().getIndex())
                        )),BitcoinAddress.class) == null) continue;
                    BitcoinTransactionOutput bTxOutput = BitcoinTransactionOutput.createBitcoinTransactionOutput(bClient,
                            txInput.getOutpoint().getHash().toString(),
                            txInput.getOutpoint().getIndex());
                    BitcoinAddress bAddress = db.findOne(Query.query(Criteria.where("address").is(bTxOutput.getAddress())), BitcoinAddress.class);
                    bAddress.removeTxOutputs(Collections.singletonList(bTxOutput));
                    save(db,bAddress);
                } catch (Exception ignore) { }
            }
            for (TransactionOutput txOutput :
                    tx.getOutputs()) {
                try {
                    if (db.findOne(Query.query(Criteria.where("address").is(
                                txOutput.getAddressFromP2PKHScript(params).toString()
                        )), BitcoinAddress.class) == null) continue;
                    BitcoinTransactionOutput bTxOutput = BitcoinTransactionOutput.createBitcoinTransactionOutput(bClient,
                            txOutput.getParentTransaction().getHashAsString(),
                            txOutput.getIndex());
                    BitcoinAddress bAddress = db.findOne(Query.query(Criteria.where("address").is(bTxOutput.getAddress())), BitcoinAddress.class);
                    bAddress.addTxOutputs(Collections.singletonList(bTxOutput));
                    save(db,bAddress);
                } catch (Exception ignore) { }
            }
        }

    }
}
