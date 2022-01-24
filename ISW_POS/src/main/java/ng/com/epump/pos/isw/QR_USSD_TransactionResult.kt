package ng.com.epump.pos.isw

import com.interswitchng.smartpos.models.transaction.cardpaycode.CardType
import com.interswitchng.smartpos.models.transaction.ussdqr.response.Transaction

class QR_USSD_TransactionResult {
    var message: String? = ""
    var transaction: Transaction? = null
    var paymentStatus: Payment_Status = Payment_Status.PENDING

    constructor(paymentStatus: Payment_Status, transaction: Transaction?, message: String?) {
        this.message = message
        this.transaction = transaction
        this.paymentStatus = paymentStatus
    }
}

enum class Payment_Status{
    COMPLETE, PENDING, ERROR, TIMEOUT
}