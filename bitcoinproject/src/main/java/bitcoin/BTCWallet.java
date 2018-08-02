package bitcoin;



import org.bitcoinj.core.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Document(collection = "WalletInfo")
public class BTCWallet {
    @Id
    private ObjectId id;
    //@Field(value="priKey")
    //private String priKey;
    @Field(value="address")
    private String address;
    @Field(value="txOuts")
    //private Map<String,Integer> txOuts;
    private List<UTxOOBj> uTxOs=new ArrayList<>();
    @Field(value="balance")
    private Long balance;

    public BTCWallet(Address address){
        super();
        this.address=address.toBase58();
        this.balance= Long.parseLong("0");
        System.out.println("Contructor 1");
        System.out.println(this.balance);
    }

    public BTCWallet(){
        super();
        this.balance= Long.parseLong("0");
        System.out.println("Contructor 2");
    }

    public BTCWallet(ObjectId id, String address, Map<String,Integer> txOuts, Long balance){
        super();
        this.id=id;
        this.address=address;
        this.balance=balance;
        System.out.println("Contructor 3");
    }


    //Getters_______________________________

    public Address getAddress() {
        return Address.fromBase58( Address.getParametersFromAddress(address), this.address);
    }

    public Long getBalance() {
        return this.balance;
    }

    public ObjectId getId() {
        return id;
    }

    public List<TransactionOutput> getuTxOs() {
        List<TransactionOutput> txOuts=new ArrayList<>();
        for (UTxOOBj uTxOOBj:
             this.uTxOs) {
            txOuts.add(App.bitcoinUtils.getTxOFromUTxO(uTxOOBj));
        }
        return txOuts;
    }

    //Setters________________________________

    public void setId(ObjectId id) {
        this.id = id;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBalance(Long balance) {
        this.balance = balance;
    }

    public void incBalance(Long value){
        this.balance+=value;
    }

    public void decBalance(Long value){
        this.balance-=value;
    }

    public void setUTxOs(List<TransactionOutput> TxOs) {
        for (TransactionOutput TxO :
                TxOs) {
            this.uTxOs.add(App.bitcoinUtils.getUTxOFromTxO(TxO));
        }
    }

    public void removeUTxOs(List<TransactionOutput> txOuts){
        for (TransactionOutput txOut :
                txOuts) {
            this.uTxOs.remove(App.bitcoinUtils.getUTxOFromTxO(txOut));
        }
    }
    /*public ECKey getPriKey() {
        ECKey _priKey= ECKey.fromPrivate(this.priKey.getBytes(),true);
        return _priKey;
    }

    public List<TransactionOutput> getTxOuts() {
        List<TransactionOutput> result=new ArrayList<>();
        for (String txid :
                this.txOuts.keySet()) {
            try {
                TransactionOutput output = App.bitcoinUtils.getClientInstance().getRawTransaction(Sha256Hash.wrap(txid)).getOutput(this.txOuts.get(txid));
                System.out.println(output);
                result.add(output);
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        return result;
    }*/

    /*public void setTxOuts(List<TransactionOutput> txOuts) {
        if (this.txOuts==null)
            this.txOuts=new HashMap<>();
        for (TransactionOutput txOutput :
                txOuts) {
            this.txOuts.put(txOutput.getParentTransaction().getHash().toString(),txOutput.getIndex());
        }
    }*/

}
