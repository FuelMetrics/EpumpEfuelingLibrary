package ng.com.epump.efueling.models;

public class Transaction {
    private String Type;
    private String Id;
    private double Amount;
    private double Volume;
    private String Time;
    private String ValueType;

    public Transaction(String type, String id, double amount, double volume, String time, String valueType) {
        Type = type;
        Id = id;
        Amount = amount;
        Volume = volume;
        Time = time;
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

    public double getAmount() {
        return Amount;
    }

    public void setAmount(double amount) {
        Amount = amount;
    }

    public double getVolume() {
        return Volume;
    }

    public void setVolume(double volume) {
        Volume = volume;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getValueType() {
        return ValueType;
    }

    public void setValueType(String valueType) {
        ValueType = valueType;
    }
}
