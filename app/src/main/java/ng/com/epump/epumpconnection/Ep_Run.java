package ng.com.epump.epumpconnection;

import com.fuelmetrics.epumpwifitool.NativeLibJava;

public class Ep_Run implements Runnable {
    private NativeLibJava _nativeLibJava;
    public Ep_Run(NativeLibJava nativeLibJava){
        _nativeLibJava = nativeLibJava;
    }
    @Override
    public void run() {
        _nativeLibJava.ep_run();
    }
}
