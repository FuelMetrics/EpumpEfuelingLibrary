package ng.com.epump.pos.isw

import com.interswitchng.smartpos.models.transaction.cardpaycode.CardType
import com.interswitchng.smartpos.models.transaction.cardpaycode.EmvMessage

public class EMVMessageProcessor {
    public fun processMessage(emvMessage: EmvMessage): MessageResult {
        var messageType = ""
        var pinText = ""
        var cardType = CardType.None
        var message = ""

        when (emvMessage){
            is EmvMessage.InsertCard -> {
                messageType = "Insert_Card"
            }
            is EmvMessage.ProcessingTransaction -> {
                messageType = "Processing_Transaction"
            }
            is EmvMessage.CardRead -> {
                messageType = "Card_Read"
                cardType = emvMessage.cardType
            }
            is EmvMessage.EnterPin -> {
                messageType = "Enter_Pin"
            }
            is EmvMessage.EmptyPin -> {
                messageType = "Empty_Pin"
            }
            is EmvMessage.IncompletePin -> {
                messageType = "Incomplete_Pin"
            }
            is EmvMessage.PinText -> {
                messageType = "Pin_Text"
                pinText = emvMessage.text
            }
            is EmvMessage.PinOk -> {
                messageType = "Pin_Ok"
            }
            is EmvMessage.PinError -> {
                messageType = "Pin_Error"
                pinText = emvMessage.remainCount.toString()
            }
            is EmvMessage.CardDetails -> {
                messageType = "Card_Details"
                cardType = emvMessage.cardType
            }
            is EmvMessage.TransactionCancelled -> {
                messageType = "Transaction_Cancelled"
                message = emvMessage.reason + " - " + emvMessage.code
            }
        }

        return MessageResult(messageType, pinText, cardType, message)
    }
}