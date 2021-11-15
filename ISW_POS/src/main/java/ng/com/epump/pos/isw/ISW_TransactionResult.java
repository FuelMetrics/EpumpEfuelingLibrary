package ng.com.epump.pos.isw;

public class ISW_TransactionResult {
    private String responseCode;
    private String responseMessage;
    private boolean isSuccessful;
    private String transactionReference;
    private long amount;
    private String cardType;
    private String transactionType;

    public ISW_TransactionResult(String responseCode, String responseMessage, boolean isSuccessful,
                                 String transactionReference, long amount, String cardType, String transactionType) {
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.isSuccessful = isSuccessful;
        this.transactionReference = transactionReference;
        this.amount = amount;
        this.cardType = cardType;
        this.transactionType = transactionType;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setSuccessful(boolean successful) {
        isSuccessful = successful;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }
}
