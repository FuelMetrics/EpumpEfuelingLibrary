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
        int ret = 0;
        do{
            ret = _nativeLibJava.ep_run();
            //Log.i("EP Run ret", "run: " + ret);
        }
        while (ret == 0);
        Log.i("Ep run", "run: task exited");
    }
}
