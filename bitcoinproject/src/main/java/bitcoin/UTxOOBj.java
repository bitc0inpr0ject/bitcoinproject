package bitcoin;

public class UTxOOBj {
    private String TransactionId;
    private int OutputIndex;

    public UTxOOBj(String transactionId, int outputIndex){
        TransactionId=transactionId;
        OutputIndex=outputIndex;
    }

    public UTxOOBj(){

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
        return (this.TransactionId.equals(otherUTxOOBj.TransactionId) && (this.OutputIndex==otherUTxOOBj.OutputIndex));
    }
}
