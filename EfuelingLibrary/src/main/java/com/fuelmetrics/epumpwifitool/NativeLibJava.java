package com.fuelmetrics.epumpwifitool;


import ng.com.epump.efueling.interfaces.JNICallbackInterface;

public class NativeLibJava {
    JNICallbackInterface cbInterface;

    static {
        try{
            System.loadLibrary("native-lib");
        }
        catch (UnsatisfiedLinkError e){
            System.out.println(e);
        }
    }

    public NativeLibJava(JNICallbackInterface cbi){
        this.cbInterface = cbi;
    }

    /** -------------------------
     * Configurtion on_child_event methods
     * ------------------------- */
    public native String passInMainCfgData(String prtcl, String prodt, String[] pnmArr, float priceArr);

    /** -------------------------
     * User on_child_event methods
     *
     * See the ep_interface.h file for
     * each API description
     * ------------------------- */
    public native int ep_init(String did, String dk);
    public native int ep_deinit();
    public native int ep_run();
    public native void ep_ms_timer();
    public native int ep_get_time_int(int ss, int mm, int hh,
                                      int dd, int mn, int yy);
    public native int ep_start_trans(String pname, int transTy,
                                     String uid, byte val_ty,
                                     float value, int time_int, String term_id);

    public native int ep_end_trans();
    public native int ep_get_cur_state();
    public native String ep_get_cur_state_string();
    public native int ep_get_pump_state();
    public native float ep_get_vol_sold();
    public native float ep_get_amo_sold();
    public native float ep_get_value();
    public native byte ep_get_value_ty();
    public native String ep_get_err_details();
    public native String ep_get_session_id();

    public native int ep_rx_data(String data, int len);

    public native Object ep_get_connection_data(String pumpname);

    /** for registering callback */
    public native int registerCallbacks();

    private void ep_trans_cb_java(String data, int len){
        this.cbInterface.tx_data(data, len);
    }


}
