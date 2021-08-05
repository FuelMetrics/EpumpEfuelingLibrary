package ng.com.epump.epumpconnection;

import android.app.Application;

import com.fuelmetrics.epumpwifitool.NativeLibJava;

public class App extends Application implements JNICallbackInterface {
    public NativeLibJava nativeLibJava;
    IData data_interface;

    public void SetupInterface(IData data){
        data_interface = data;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        nativeLibJava = new NativeLibJava(this);
    }

    @Override
    public void tx_data(String data, int len) {
        //tx_data(data, len);
        data_interface.tx_data(data, len);
    }
}
