package ng.com.epump.efueling.models;

public enum GO_TransactionType {
    Undefined("Unknown"),
    Voucher("Voucher"),
    Remis("Remis"),
    Card("Card"),
    POS("POS"),
    Attendant("Attendant"),
    Offline_Attendant("Offline"),
    Last("");

    public final String label;

    private GO_TransactionType(String label){
        this.label = label;
    }

    public static String get(int index){
        return values()[index].label;
    }
}

