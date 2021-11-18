package ng.com.epump.pos.isw;

public class PrintTransactionModel {
    private String stationName;
    private String stationAddress;
    private String transactionId;
    private String date;
    private String time;
    private String transactionChannel;
    private String product;
    private String amount;
    private String volume;
    private String voucherCardNumber;
    private boolean customerCopy;

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStationAddress() {
        return stationAddress;
    }

    public void setStationAddress(String stationAddress) {
        this.stationAddress = stationAddress;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTransactionChannel() {
        return transactionChannel;
    }

    public void setTransactionChannel(String transactionChannel) {
        this.transactionChannel = transactionChannel;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getVoucherCardNumber() {
        return voucherCardNumber;
    }

    public void setVoucherCardNumber(String voucherCardNumber) {
        this.voucherCardNumber = voucherCardNumber;
    }

    public boolean isCustomerCopy() {
        return customerCopy;
    }

    public void setCustomerCopy(boolean customerCopy) {
        this.customerCopy = customerCopy;
    }

    public String getCopyTitle(){
        return this.isCustomerCopy() ? "Customer's Copy" : "Merchant's Copy";
    }
}
