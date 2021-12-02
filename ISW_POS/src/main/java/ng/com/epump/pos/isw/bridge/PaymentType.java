package ng.com.epump.pos.isw.bridge;

public enum PaymentType {
    PayCode,

    Card,

    QR,

    USSD;

    @Override
    public String toString() {
        return this.name();
    }
}
