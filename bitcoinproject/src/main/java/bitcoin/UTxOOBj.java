package bitcoin;

/**
 * Custom Unspent Transaction Output Object
 *
 */
public class UTxOOBj {
    private String TransactionId;
    private int OutputIndex;
    private boolean IsSpending;

    public UTxOOBj(String transactionId, int outputIndex, boolean isSpending){
        TransactionId=transactionId;
        OutputIndex=outputIndex;
        IsSpending=isSpending;
    }

    public UTxOOBj(){

    }

    public boolean isSpending() {
        return IsSpending;
    }

    public void setSpending(boolean spending) {
        IsSpending = spending;
    }

    public int getOutputIndex() {
        return OutputIndex;
    }

    public String getTransactionId() {
        return TransactionId;
    }

    public void setOutputIndex(int outputIndex) {
        OutputIndex = outputIndex;
    }

    public void setTransactionId(String transactionId) {
        TransactionId = transactionId;
    }

    @Override
    public boolean equals(Object var1){
        if (var1 == null) return false;
        if (var1 == this) return true;
        if (!(var1 instanceof UTxOOBj)) return false;
        UTxOOBj otherUTxOOBj = (UTxOOBj)var1;
        return (this.TransactionId.equals(otherUTxOOBj.TransactionId) && (this.OutputIndex==otherUTxOOBj.OutputIndex) && (this.IsSpending==otherUTxOOBj.IsSpending));
    }
}
