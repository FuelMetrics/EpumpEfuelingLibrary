package ng.com.epump.pos.isw.bridge;

public class TransactionResult {
    private String AID;
    private String amount;
    private String authorizationCode;
    private String cardExpiry;
    private String cardPan;
    private String cardPin;
    private String cardTrack2;
    private CardType cardType;
    private String code;
    private String csn;
    private String dateTime;
    private int hasPrintedCustomerCopy;
    private int hasPrintedMerchantCopy;
    private String icc;
    private String month;
    private String name;
    private String originalTransmissionDateTime;
    private PaymentType paymentType;
    private String pinStatus;
    private String ref;
    private String responseCode;
    private String responseMessage;
    private String rrn;
    private String src;
    private String stan;
    private String telephone;
    private String time;
    private TransactionType type;

    public TransactionResult(){}

    public TransactionResult(String AID, String amount, String authorizationCode, String cardExpiry,
                             String cardPan, String cardPin, String cardTrack2, CardType cardType,
                             String code, String csn, String dateTime, int hasPrintedCustomerCopy,
                             int hasPrintedMerchantCopy, String icc, String month, String name,
                             String originalTransmissionDateTime, PaymentType paymentType,
                             String pinStatus, String ref, String responseCode, String responseMessage,
                             String rrn, String src, String stan, String telephone, String time,
                             TransactionType type) {
        this.AID = AID;
        this.amount = amount;
        this.authorizationCode = authorizationCode;
        this.cardExpiry = cardExpiry;
        this.cardPan = cardPan;
        this.cardPin = cardPin;
        this.cardTrack2 = cardTrack2;
        this.cardType = cardType;
        this.code = code;
        this.csn = csn;
        this.dateTime = dateTime;
        this.hasPrintedCustomerCopy = hasPrintedCustomerCopy;
        this.hasPrintedMerchantCopy = hasPrintedMerchantCopy;
        this.icc = icc;
        this.month = month;
        this.name = name;
        this.originalTransmissionDateTime = originalTransmissionDateTime;
        this.paymentType = paymentType;
        this.pinStatus = pinStatus;
        this.ref = ref;
        this.responseCode = responseCode;
        this.responseMessage = responseMessage;
        this.rrn = rrn;
        this.src = src;
        this.stan = stan;
        this.telephone = telephone;
        this.time = time;
        this.type = type;
    }

    public String getAID() {
        return AID;
    }

    public void setAID(String AID) {
        this.AID = AID;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public void setAuthorizationCode(String authorizationCode) {
        this.authorizationCode = authorizationCode;
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }

    public String getCardPan() {
        return cardPan;
    }

    public void setCardPan(String cardPan) {
        this.cardPan = cardPan;
    }

    public String getCardPin() {
        return cardPin;
    }

    public void setCardPin(String cardPin) {
        this.cardPin = cardPin;
    }

    public String getCardTrack2() {
        return cardTrack2;
    }

    public void setCardTrack2(String cardTrack2) {
        this.cardTrack2 = cardTrack2;
    }

    public CardType getCardType() {
        return cardType;
    }

    public void setCardType(CardType cardType) {
        this.cardType = cardType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCsn() {
        return csn;
    }

    public void setCsn(String csn) {
        this.csn = csn;
    }

    public String getDateTime() {
        return dateTime;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public int getHasPrintedCustomerCopy() {
        return hasPrintedCustomerCopy;
    }

    public void setHasPrintedCustomerCopy(int hasPrintedCustomerCopy) {
        this.hasPrintedCustomerCopy = hasPrintedCustomerCopy;
    }

    public int getHasPrintedMerchantCopy() {
        return hasPrintedMerchantCopy;
    }

    public void setHasPrintedMerchantCopy(int hasPrintedMerchantCopy) {
        this.hasPrintedMerchantCopy = hasPrintedMerchantCopy;
    }

    public String getIcc() {
        return icc;
    }

    public void setIcc(String icc) {
        this.icc = icc;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalTransmissionDateTime() {
        return originalTransmissionDateTime;
    }

    public void setOriginalTransmissionDateTime(String originalTransmissionDateTime) {
        this.originalTransmissionDateTime = originalTransmissionDateTime;
    }

    public PaymentType getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(PaymentType paymentType) {
        this.paymentType = paymentType;
    }

    public String getPinStatus() {
        return pinStatus;
    }

    public void setPinStatus(String pinStatus) {
        this.pinStatus = pinStatus;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
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

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}
