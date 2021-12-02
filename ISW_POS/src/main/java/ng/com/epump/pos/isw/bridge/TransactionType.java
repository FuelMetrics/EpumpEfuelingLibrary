package ng.com.epump.pos.isw.bridge;

public enum TransactionType {
    Purchase,

    PreAuth,

    Completion,

    Refund,

    Reversal,

    CardNotPresent,

    BillPayment,

    CashOutInquiry,

    CashOutPay,

    Transfer,

    AirtimeRecharge,

    Balance,

    PinSelect,

    Deposit,

    Withdrawal;
}
