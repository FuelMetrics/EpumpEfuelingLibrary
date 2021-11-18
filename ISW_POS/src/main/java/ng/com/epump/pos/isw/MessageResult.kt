package ng.com.epump.pos.isw

import com.interswitchng.smartpos.models.transaction.cardpaycode.CardType

class MessageResult {
    var messageType: String = ""
    var pinText: String = ""
    var message: String = ""
    var cardType: CardType = CardType.None

    constructor(messageType: String, pinText: String, cardType: CardType) {
        this.messageType = messageType
        this.pinText = pinText
        this.cardType = cardType
    }

    constructor(messageType: String, pinText: String, cardType: CardType, message: String) {
        this.messageType = messageType
        this.pinText = pinText
        this.cardType = cardType
        this.message = message
    }
}