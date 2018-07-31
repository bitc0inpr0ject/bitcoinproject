package BitcoinService;

import BitcoinModel.BitcoinTransactionOutput;
import BitcoinModel.BitcoinWallet;
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

public class BitcoinWalletService {
    public static BitcoinWallet load(MongoTemplate db, String address) {
        return db.findOne(Query.query(Criteria.where("address").is(address)), BitcoinWallet.class);
    }

    public static void save(MongoTemplate db, BitcoinWallet wallet) {
        db.findAndRemove(Query.query(Criteria.where("address").is(wallet.getAddress())), BitcoinWallet.class);
        db.save(wallet);
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
                    )),BitcoinWallet.class) == null) continue;
                    BitcoinTransactionOutput bTxOutput = BitcoinTransactionOutput.createBitcoinTransactionOutput(bClient,
                            txInput.getOutpoint().getHash().toString(),
                            txInput.getOutpoint().getIndex());
                    BitcoinWallet bWallet = db.findOne(Query.query(Criteria.where("address").is(bTxOutput.getAddress())), BitcoinWallet.class);
                    bWallet.removeTxOutputs(Collections.singletonList(bTxOutput));
                    save(db,bWallet);
                } catch (Exception ignore) { }
            }
            for (TransactionOutput txOutput :
                    tx.getOutputs()) {
                try {
                    if (db.findOne(Query.query(Criteria.where("address").is(
                            txOutput.getAddressFromP2SH(params).toString()
                    )),BitcoinWallet.class) == null) continue;
                    if (db.findOne(Query.query(Criteria.where("txOutputs").elemMatch(
                            Criteria.where("txHash").is(txOutput.getParentTransaction().getHashAsString())
                                    .and("index").is(txOutput.getIndex())
                    )),BitcoinWallet.class) != null) continue;
                    BitcoinTransactionOutput bTxOutput = BitcoinTransactionOutput.createBitcoinTransactionOutput(bClient,
                            txOutput.getParentTransaction().getHashAsString(),
                            txOutput.getIndex());
                    BitcoinWallet bWallet = db.findOne(Query.query(Criteria.where("address").is(bTxOutput.getAddress())), BitcoinWallet.class);
                    bWallet.addTxOutputs(Collections.singletonList(bTxOutput));
                    save(db,bWallet);
                } catch (Exception ignore) { }
            }
        }
    }
}
