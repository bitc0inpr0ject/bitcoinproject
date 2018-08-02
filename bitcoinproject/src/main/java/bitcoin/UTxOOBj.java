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
}
