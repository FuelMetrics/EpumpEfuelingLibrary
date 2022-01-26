package ng.com.epump.pos.isw

import com.interswitchng.smartpos.models.transaction.ussdqr.response.PaymentStatus
import com.interswitchng.smartpos.models.transaction.ussdqr.response.Transaction
import ng.com.epump.pos.isw.bridge.Isw_Transaction

public class QR_USSD_PaymentStatus {
    public fun getResponse(paymentStatus: PaymentStatus): QR_USSD_TransactionResult? {
        var message: String? = ""
        var transaction: Transaction? = null
        var status: Payment_Status = Payment_Status.PENDING

        when (paymentStatus){
            is PaymentStatus.Complete -> {
                transaction = paymentStatus.transaction
                message = "Transaction Complete"
                status = Payment_Status.COMPLETE
            }
            is PaymentStatus.Error -> {
                transaction = paymentStatus.transaction
                message = paymentStatus.errorMsg
                status = Payment_Status.ERROR
            }
            is PaymentStatus.Pending -> {
                transaction = paymentStatus.transaction
                message = "Transaction Pending"
                status = Payment_Status.PENDING
            }
            is PaymentStatus.Timeout -> {
                message = "Transaction Timeout"
                status = Payment_Status.TIMEOUT
            }
            is PaymentStatus.OngoingTimeout -> {
                message = "Transaction Timeout"
                status = Payment_Status.TIMEOUT
            }
        }

        return  QR_USSD_TransactionResult(status, Isw_Transaction.getTransaction(transaction), message)
    }
}