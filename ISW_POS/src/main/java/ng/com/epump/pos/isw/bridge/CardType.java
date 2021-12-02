package ng.com.epump.pos.isw.bridge;

public enum CardType {
    MASTER,

    VISA,

    VERVE,

    AMERICANEXPRESS,

    CHINAUNIONPAY,

    None;

    @Override
    public String toString() {
        return this.name();
    }
}
