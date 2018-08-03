package BitcoinService;

import BitcoinModel.BitcoinAddress;
import BitcoinModel.BitcoinTransactionOutput;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import javafx.util.Pair;
import org.bitcoinj.core.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BitcoinAddressService {
    public static BitcoinAddress load(MongoTemplate db, String address) {
        return db.findOne(Query.query(Criteria.where("address").is(address)), BitcoinAddress.class);
    }

    public static void save(MongoTemplate db, BitcoinAddress address) {
        db.findAndRemove(Query.query(Criteria.where("address").is(address.getAddress())), BitcoinAddress.class);
        db.save(address);
    }

    public static void update(MongoTemplate db, BitcoinClient client, int currentBlock, int confirmations) throws IOException {
        NetworkParameters params = client.getNetParams();
        for (TransactionInput txInput :
                BitcoinUtils.getTransactionInputInBlock(client,currentBlock)) {
            try {
                BitcoinAddress bAddress = db.findOne(Query.query(Criteria.where("txOutputs").elemMatch(
                        Criteria.where("txHash").is(txInput.getOutpoint().getHash().toString())
                                .and("index").is(txInput.getOutpoint().getIndex()))),
                        BitcoinAddress.class);
                if (bAddress == null) continue;
                BitcoinTransactionOutput bTxOutput = BitcoinTransactionOutput.createBitcoinTransactionOutput(client,
                        txInput.getOutpoint().getHash().toString(),
                        txInput.getOutpoint().getIndex());
                bAddress.removeTxOutputs(Collections.singletonList(bTxOutput));
                save(db,bAddress);
            } catch (Exception ignore) { }
        }
        if (confirmations < 1) return;
        for (TransactionOutput txOutput :
                BitcoinUtils.getTransactionOutputInBlock(client,currentBlock-confirmations+1)) {
            try {
                if (txOutput.getScriptPubKey().getToAddress(params) == null) continue;
                BitcoinAddress bAddress = db.findOne(Query.query(Criteria.where("address").is(
                        txOutput.getScriptPubKey().getToAddress(params).toString())),
                        BitcoinAddress.class);
                if (bAddress == null) continue;
                if (db.findOne(Query.query(Criteria.where("txOutputs").elemMatch(
                        Criteria.where("txHash").is(txOutput.getParentTransaction().getHashAsString())
                                .and("index").is(txOutput.getIndex()))),
                        BitcoinAddress.class) != null) continue;
                BitcoinTransactionOutput bTxOutput = BitcoinTransactionOutput.createBitcoinTransactionOutput(client,
                        txOutput.getParentTransaction().getHashAsString(),
                        txOutput.getIndex());
                bAddress.addTxOutputs(Collections.singletonList(bTxOutput));
                save(db,bAddress);
            } catch (Exception ignore) { }
        }
    }

    public static Transaction sendToAddresses(MongoTemplate db, String from, BitcoinClient client, List<Pair<Address,Coin>> candidates, Coin feePerKb) throws InsufficientMoneyException {
        try {
            NetworkParameters params = client.getNetParams();
            BitcoinAddress bAddress = load(db,from);
            if (bAddress == null) return null;
            List<TransactionOutput> txOutputs = BitcoinTransactionOutput.getTransactionOutputList(client,bAddress.getTxOutputs());
            Transaction tx = BitcoinUtils.sendToAddressesByPrivKey(
                    params,
                    txOutputs,
                    DumpedPrivateKey.fromBase58(params,bAddress.getPrivKey()).getKey(),
                    candidates, feePerKb);
            List<BitcoinTransactionOutput> bTxOutputs = new ArrayList<>();
            for (BitcoinTransactionOutput bTxOutput :
                    bAddress.getTxOutputs()) {
                boolean chk = false;
                for (TransactionInput txInput :
                        tx.getInputs()) {
                    if (bTxOutput.getTxHash().equals(txInput.getOutpoint().getHash().toString())
                            && bTxOutput.getIndex() == txInput.getOutpoint().getIndex()) {
                        chk = true;
                        break;
                    }
                }
                if (chk) continue;
                bTxOutputs.add(bTxOutput);
            }
            bAddress.setTxOutputs(bTxOutputs);
            save(db,bAddress);
            return tx;
        } catch (InsufficientMoneyException e) {
            throw e;
        } catch (Exception ignore) {
            return null;
        }
    }
}
