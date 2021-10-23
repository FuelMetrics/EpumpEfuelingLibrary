package ng.com.epump.pos.isw;

public interface PaymentCallback {
    void onCancel();
    void onPaymentComplete(ISW_TransactionResult iswTransactionResult);
}
