package ng.com.epump.pos.isw.bridge;

import com.interswitchng.smartpos.models.transaction.ussdqr.response.Transaction;

public class Isw_Transaction {
    private int amount;
    private String bankCode;
    private String currencyCode;
    private int id;
    private boolean paymentCancelled;
    private int remittanceAmount;
    private String responseCode;
    private String responseDescription;
    private String transactionReference;

    public static Isw_Transaction getTransaction(Transaction transaction){
        return new Isw_Transaction(transaction.getAmount$smart_pos_core_release(),
                transaction.getBankCode$smart_pos_core_release(),
                transaction.getCurrencyCode$smart_pos_core_release(), transaction.getId$smart_pos_core_release(),
                transaction.getPaymentCancelled$smart_pos_core_release(), transaction.getRemittanceAmount$smart_pos_core_release(),
                transaction.getResponseCode$smart_pos_core_release(), transaction.getResponseDescription(),
                transaction.getTransactionReference$smart_pos_core_release());
    }

    public Isw_Transaction(int amount, String bankCode, String currencyCode, int id, boolean paymentCancelled, int remittanceAmount, String responseCode, String responseDescription, String transactionReference) {
        this.amount = amount;
        this.bankCode = bankCode;
        this.currencyCode = currencyCode;
        this.id = id;
        this.paymentCancelled = paymentCancelled;
        this.remittanceAmount = remittanceAmount;
        this.responseCode = responseCode;
        this.responseDescription = responseDescription;
        this.transactionReference = transactionReference;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isPaymentCancelled() {
        return paymentCancelled;
    }

    public void setPaymentCancelled(boolean paymentCancelled) {
        this.paymentCancelled = paymentCancelled;
    }

    public int getRemittanceAmount() {
        return remittanceAmount;
    }

    public void setRemittanceAmount(int remittanceAmount) {
        this.remittanceAmount = remittanceAmount;
    }

    public String getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(String responseCode) {
        this.responseCode = responseCode;
    }

    public String getResponseDescription() {
        return responseDescription;
    }

    public void setResponseDescription(String responseDescription) {
        this.responseDescription = responseDescription;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
}
