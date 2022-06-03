package ng.com.epump.pos.isw.bridge;

public class EmvData {
    private String AID;
    private String cardExpiry;
    private String cardPAN;
    private String cardPIN;
    private String cardTrack2;
    private String csn;
    private String pinKsn;
    private String src;
    private IccData icc;

    public EmvData(String AID, String cardExpiry, String cardPAN, String cardPIN,
                   String cardTrack2, String csn, String pinKsn, String src, IccData icc) {
        this.AID = AID;
        this.cardExpiry = cardExpiry;
        this.cardPAN = cardPAN;
        this.cardPIN = cardPIN;
        this.cardTrack2 = cardTrack2;
        this.csn = csn;
        this.pinKsn = pinKsn;
        this.src = src;
        this.icc = icc;
    }

    public String getAID() {
        return AID;
    }

    public void setAID(String AID) {
        this.AID = AID;
    }

    public String getCardExpiry() {
        return cardExpiry;
    }

    public void setCardExpiry(String cardExpiry) {
        this.cardExpiry = cardExpiry;
    }

    public String getCardPAN() {
        return cardPAN;
    }

    public void setCardPAN(String cardPAN) {
        this.cardPAN = cardPAN;
    }

    public String getCardPIN() {
        return cardPIN;
    }

    public void setCardPIN(String cardPIN) {
        this.cardPIN = cardPIN;
    }

    public String getCardTrack2() {
        return cardTrack2;
    }

    public void setCardTrack2(String cardTrack2) {
        this.cardTrack2 = cardTrack2;
    }

    public String getCsn() {
        return csn;
    }

    public void setCsn(String csn) {
        this.csn = csn;
    }

    public String getPinKsn() {
        return pinKsn;
    }

    public void setPinKsn(String pinKsn) {
        this.pinKsn = pinKsn;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public IccData getIcc() {
        return icc;
    }

    public void setIcc(IccData icc) {
        this.icc = icc;
    }
}
