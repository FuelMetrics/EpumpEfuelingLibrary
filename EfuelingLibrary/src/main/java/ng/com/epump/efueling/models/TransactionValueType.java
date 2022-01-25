package ng.com.epump.efueling.models;

public enum TransactionValueType {
    Amount("Amount"), Volume("Volume");

    public final String label;

    private TransactionValueType(String label){
        this.label = label;
    }

    public static String get(int index){
        index = (index == 0 || index == 97) ? 0 : 1;
        return values()[index].label;
    }
}
