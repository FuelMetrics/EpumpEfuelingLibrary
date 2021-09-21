package ng.com.epump.efueling.models;

public enum TransactionType {
    Voucher("Voucher"),
    Remis("Remis"),
    Card("Card"),
    POS("POS"),
    Offline_Voucher("Offline Voucher"),
    Offline_Remis("Offline Remis"),
    Offline_Card("Offline Card"),
    Offline_POS("Offline POS"),
    Resume_Transaction("Resume Transaction");

    public final String label;

    private TransactionType(String label){
        this.label = label;
    }

    public static String get(int index){
        return values()[index].label;
    }
}

