package ng.com.epump.efueling.models;

public enum TransactionValueType {
    Amount("Amount"), Volume("Volume");

    public final String label;

    private TransactionValueType(String label){
        this.label = label;
    }

    public static String get(int index){
        return values()[index].label;
    }
}
