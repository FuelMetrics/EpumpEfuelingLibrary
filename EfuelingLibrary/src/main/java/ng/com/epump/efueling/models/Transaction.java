package ng.com.epump.efueling.models;

public class Transaction {
    private String Type;
    private String Id;
    private double Value;
    private String ValueType;

    public Transaction(String type, String id, double value, String valueType) {
        Type = type;
        Id = id;
        Value = value;
        ValueType = valueType;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public double getValue() {
        return Value;
    }

    public void setValue(double value) {
        Value = value;
    }

    public String getValueType() {
        return ValueType;
    }

    public void setValueType(String valueType) {
        ValueType = valueType;
    }
}
