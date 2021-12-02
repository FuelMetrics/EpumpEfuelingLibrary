package ng.com.epump.pos.isw.bridge;

public class TransactionResponse {
    private String authCode;
    private String availableBalance;
    private String ledgerBalance;
    private String month;
    private String name;
    private String ref;
    private String responseCode;
    private String responseDescription;
    private String rrn;
    private String scripts;
    private String stan;
    private String time;
    private String transmissionDateTime;
    private TransactionType type;

    public TransactionResponse(String authCode, String availableBalance, String ledgerBalance,
                               String month, String name, String ref, String responseCode,
                               String responseDescription, String rrn, String scripts,
                               String stan, String time, String transmissionDateTime,
                               TransactionType type) {
        this.authCode = authCode;
        this.availableBalance = availableBalance;
        this.ledgerBalance = ledgerBalance;
        this.month = month;
        this.name = name;
        this.ref = ref;
        this.responseCode = responseCode;
        this.responseDescription = responseDescription;
        this.rrn = rrn;
        this.scripts = scripts;
        this.stan = stan;
        this.time = time;
        this.transmissionDateTime = transmissionDateTime;
        this.type = type;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public String getAvailableBalance() {
        return availableBalance;
    }

    public void setAvailableBalance(String availableBalance) {
        this.availableBalance = availableBalance;
    }

    public String getLedgerBalance() {
        return ledgerBalance;
    }

    public void setLedgerBalance(String ledgerBalance) {
        this.ledgerBalance = ledgerBalance;
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

    public String getResponseDescription() {
        return responseDescription;
    }

    public void setResponseDescription(String responseDescription) {
        this.responseDescription = responseDescription;
    }

    public String getRrn() {
        return rrn;
    }

    public void setRrn(String rrn) {
        this.rrn = rrn;
    }

    public String getScripts() {
        return scripts;
    }

    public void setScripts(String scripts) {
        this.scripts = scripts;
    }

    public String getStan() {
        return stan;
    }

    public void setStan(String stan) {
        this.stan = stan;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTransmissionDateTime() {
        return transmissionDateTime;
    }

    public void setTransmissionDateTime(String transmissionDateTime) {
        this.transmissionDateTime = transmissionDateTime;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }
}
