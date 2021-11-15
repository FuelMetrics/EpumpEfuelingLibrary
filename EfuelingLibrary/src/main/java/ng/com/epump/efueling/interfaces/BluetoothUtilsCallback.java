package ng.com.epump.efueling.interfaces;

public interface BluetoothUtilsCallback {
    void onConnected();
    void onRead(String data);
}
