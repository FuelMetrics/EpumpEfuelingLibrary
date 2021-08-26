package ng.com.epump.efueling.models;

import android.util.Log;

import com.fuelmetrics.epumpwifitool.NativeLibJava;

public class Ep_Run implements Runnable {
    private NativeLibJava _nativeLibJava;
    public Ep_Run(NativeLibJava nativeLibJava){
        _nativeLibJava = nativeLibJava;
    }
    @Override
    public void run() {
        Log.i("Ep run", "run: task started");
        _nativeLibJava.ep_run();
        Log.i("Ep run", "run: task exited");
    }
}
