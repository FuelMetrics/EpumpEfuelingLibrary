package ng.com.epump.pos.isw

import ng.com.epump.pos.isw.bridge.Isw_Transaction


class QR_USSD_TransactionResult {
    var message: String? = ""
    var transaction: Isw_Transaction? = null
    var paymentStatus: Payment_Status = Payment_Status.PENDING

    constructor(paymentStatus: Payment_Status, transaction: Isw_Transaction?, message: String?) {
        this.message = message
        this.transaction = transaction
        this.paymentStatus = paymentStatus
    }
}

enum class Payment_Status{
    COMPLETE, PENDING, ERROR, TIMEOUT
}