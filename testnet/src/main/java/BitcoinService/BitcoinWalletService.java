package BitcoinService;

import BitcoinModel.BitcoinTransactionOutput;
import BitcoinModel.BitcoinWallet;
import com.msgilligan.bitcoinj.rpc.BitcoinClient;
import javafx.util.Pair;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.TransactionSignature;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BitcoinWalletService {
    public static BitcoinWallet load(MongoTemplate db, String address) {
        return db.findOne(Query.query(Criteria.where("address").is(address)), BitcoinWallet.class);
    }

    public static void save(MongoTemplate db, BitcoinWallet wallet) {
        db.findAndRemove(Query.query(Criteria.where("address").is(wallet.getAddress())), BitcoinWallet.class);
        db.save(wallet);
    }

    public static void update(MongoTemplate db, BitcoinClient client, int currentBlock, int confirmations) throws IOException {
        NetworkParameters params = client.getNetParams();
        for (TransactionInput txInput :
                BitcoinUtils.getTransactionInputInBlock(client,currentBlock)) {
            try {
                if (db.findOne(Query.query(Criteria.where("txOutputs").elemMatch(
                        Criteria.where("txHash").is(txInput.getOutpoint().getHash().toString())
                                .and("index").is(txInput.getOutpoint().getIndex()))),
                        BitcoinWallet.class) == null) continue;
                BitcoinTransactionOutput bTxOutput = BitcoinTransactionOutput.createBitcoinTransactionOutput(client,
                        txInput.getOutpoint().getHash().toString(),
                        txInput.getOutpoint().getIndex());
                BitcoinWallet bAddress = db.findOne(Query.query(Criteria.where("address").is(bTxOutput.getAddress())),BitcoinWallet.class);
                bAddress.removeTxOutputs(Collections.singletonList(bTxOutput));
                save(db,bAddress);
            } catch (Exception ignore) { }
        }
        if (confirmations < 1) return;
        for (TransactionOutput txOutput :
                BitcoinUtils.getTransactionOutputInBlock(client,currentBlock-confirmations+1)) {
            try {
                if (txOutput.getScriptPubKey().getToAddress(params) == null) continue;
                if (db.findOne(Query.query(Criteria.where("address").is(
                        txOutput.getScriptPubKey().getToAddress(params).toString())),
                        BitcoinWallet.class) == null) continue;
                if (db.findOne(Query.query(Criteria.where("txOutputs").elemMatch(
                        Criteria.where("txHash").is(txOutput.getParentTransaction().getHashAsString())
                                .and("index").is(txOutput.getIndex()))),
                        BitcoinWallet.class) != null) continue;
                BitcoinTransactionOutput bTxOutput = BitcoinTransactionOutput.createBitcoinTransactionOutput(client,
                        txOutput.getParentTransaction().getHashAsString(),
                        txOutput.getIndex());
                BitcoinWallet bAddress = db.findOne(Query.query(Criteria.where("address").is(bTxOutput.getAddress())),BitcoinWallet.class);
                bAddress.addTxOutputs(Collections.singletonList(bTxOutput));
                save(db,bAddress);
            } catch (Exception ignore) { }
        }
    }

    public static Transaction createRawTx(MongoTemplate db, String from, BitcoinClient client, List<Pair<Address,Coin>> candidates, Coin feePerKb) throws InsufficientMoneyException {
        try {
            NetworkParameters params = client.getNetParams();
            BitcoinWallet wallet = load(db,from);
            if (wallet == null) return null;
            Transaction tx = BitcoinUtils.create2of3MultiSigRawTx(
                    params,
                    BitcoinTransactionOutput.getTransactionOutputList(client,wallet.getTxOutputs()),
                    wallet.getRedeemScript(),
                    candidates,
                    feePerKb);
            return tx;
        } catch (InsufficientMoneyException e) {
            throw e;
        } catch (Exception ignore) {
            return null;
        }
    }
    public static List<Sha256Hash> createRawTxHashes(MongoTemplate db, String from, Transaction rawTx) {
        try {
            BitcoinWallet wallet = load(db,from);
            if (wallet == null) return null;
            return BitcoinUtils.create2of3MultiSigRawTxHash(rawTx,wallet.getRedeemScript());
        } catch (Exception ignore) {
            return null;
        }
    }
    public static Transaction signAndSendTx(MongoTemplate db, String from, BitcoinClient client, Transaction rawTx, List<TransactionSignature> userTxSign) {
        try {
            NetworkParameters params = client.getNetParams();
            BitcoinWallet wallet = load(db,from);
            rawTx = BitcoinUtils.signRaw2of3MultiSigTransaction(
                    rawTx,
                    wallet.getRedeemScript(),
                    userTxSign,
                    DumpedPrivateKey.fromBase58(params,wallet.getServerPrivKey()).getKey());
            client.sendRawTransaction(rawTx);
            List<TransactionOutput> txOutputs = rawTx.getOutputs();
            List<BitcoinTransactionOutput> bTxOutputs = new ArrayList<>();
            for (BitcoinTransactionOutput bTxOutput :
                    wallet.getTxOutputs()) {
                boolean chk = false;
                for (TransactionOutput txOutput :
                        txOutputs) {
                    if (bTxOutput.getTxHash().equals(txOutput.getParentTransaction().getHashAsString())
                            && bTxOutput.getIndex() == txOutput.getIndex()) {
                        chk = true;
                        break;
                    }
                }
                if (chk) continue;
                bTxOutputs.add(bTxOutput);
            }
            wallet.setTxOutputs(bTxOutputs);
            save(db,wallet);
            return rawTx;
        } catch (Exception ignore) {
            return null;
        }
    }
}
