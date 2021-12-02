package ng.com.epump.pos.isw.bridge;

public class CardTransactionResponse {
    private EmvData emvData;
    private CardProcessResult onlineProcessResult;
    private TransactionResponse transactionResponse;
    private TransactionResult transactionResult;

    public CardTransactionResponse(EmvData emvData, CardProcessResult onlineProcessResult,
                                   TransactionResponse transactionResponse,
                                   TransactionResult transactionResult) {
        this.emvData = emvData;
        this.onlineProcessResult = onlineProcessResult;
        this.transactionResponse = transactionResponse;
        this.transactionResult = transactionResult;
    }

    public EmvData getEmvData() {
        return emvData;
    }

    public void setEmvData(EmvData emvData) {
        this.emvData = emvData;
    }

    public CardProcessResult getOnlineProcessResult() {
        return onlineProcessResult;
    }

    public void setOnlineProcessResult(CardProcessResult onlineProcessResult) {
        this.onlineProcessResult = onlineProcessResult;
    }

    public TransactionResponse getTransactionResponse() {
        return transactionResponse;
    }

    public void setTransactionResponse(TransactionResponse transactionResponse) {
        this.transactionResponse = transactionResponse;
    }

    public TransactionResult getTransactionResult() {
        return transactionResult;
    }

    public void setTransactionResult(TransactionResult transactionResult) {
        this.transactionResult = transactionResult;
    }
}
