package ng.com.epump.efueling;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fuelmetrics.epumpwifitool.NativeLibJava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;

import ng.com.epump.efueling.interfaces.IData;
import ng.com.epump.efueling.interfaces.JNICallbackInterface;
import ng.com.epump.efueling.models.Ep_Run;
import ng.com.epump.efueling.models.TransactionType;
import ng.com.epump.efueling.models.ValueType;
import ng.com.epump.efueling.ui.TransactionActivity;

public class EfuelingConnect implements JNICallbackInterface {
    public static final int TRANSACTION_START = 213;
    public NativeLibJava nativeLibJava;
    @SuppressLint("StaticFieldLeak")
    private static EfuelingConnect _connect;
    private Context mContext;
    private IData data_interface;
    private WifiManager wifiManager;
    private boolean runCalled;
    private PrintWriter output;
    private Socket socket;
    private CountDownTimer countDownTimer;
    private int wifiAvailability = 1;
    private boolean disposed;
    private Activity activity;
    private String mDailyKey;
    private String mTerminalId = "2101LH95";

    private EfuelingConnect(Context context){
        this.mContext = context;
        if (context instanceof Activity){
            this.activity = (Activity) context;
        }
    }

    public static EfuelingConnect getInstance(Context context){
        if (_connect == null){
            _connect = new EfuelingConnect(context);
        }
        return _connect;
    }

    public void init(String dailyKey, String... terminalId){
        mDailyKey = dailyKey;
        if (terminalId.length > 0 && !terminalId[0].isEmpty()){
            mTerminalId = terminalId[0];
        }
        if (disposed){
            disposed = false;
        }
        if (nativeLibJava != null){
            nativeLibJava.ep_end_trans();
        }
        nativeLibJava = new NativeLibJava(this);
        data_interface = (IData) mContext;
    }

    @Override
    public void tx_data(String data, int len) {
        if (output != null){
            output.println(data);
            output.flush();
        }

        /*Intent intent = new Intent("wifi_state");
        intent.putExtra("pump_state", nativeLibJava.ep_get_pump_state());
        intent.putExtra("transaction_state", nativeLibJava.ep_get_cur_state());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        data_interface.tx_data(data, len);*/
    }

    public void turnWifi(final boolean state){
        new Thread(new Runnable() {
            @Override
            public void run() {
                wifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (wifiManager.isWifiEnabled() != state) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                        ((Activity)mContext).startActivityForResult(panelIntent, 223);
                    } else {
                        wifiManager.setWifiEnabled(state);
                    }
                }
            }
        }).start();
    }

    public void connect2WifiAndSocket(String ssid, String password, final String ipAddress){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = "\"" + ssid + "\"";
                wifiConfig.preSharedKey = "\"" + password + "\"";
                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();
                /*if (isWifiConnected("\"" + deviceId + "\"")) {
                    doSomethingHere()
                }*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier.Builder()
                    .setSsid(ssid)
                    .setWpa2Passphrase(password)
                    .build();

            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(wifiNetworkSpecifier)
                    .build();

            final ConnectivityManager connectivityManager = (ConnectivityManager)mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    wifiAvailability = 0;
                    connectivityManager.bindProcessToNetwork(network);
                    nativeLibJava.registerCallbacks();
                    int res = nativeLibJava.ep_init("", mDailyKey);
                    if (res == 0){
                        data_interface.initComplete(true);
                        /*runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnMessage.setEnabled(true);
                            }
                        });*/
                    }
                    else {
                        data_interface.initComplete(false);
                    }

                    countDownTimer = new CountDownTimer(60000, 100) {
                        @Override
                        public void onTick(long l) {
                            nativeLibJava.ep_ms_timer();
                            //data_interface.getStates(nativeLibJava.ep_get_pump_state(), nativeLibJava.ep_get_cur_state());
                            Intent intent = new Intent("get_States");
                            intent.putExtra("pump_state", nativeLibJava.ep_get_pump_state());
                            intent.putExtra("transaction_state", nativeLibJava.ep_get_cur_state());
                            intent.putExtra("transaction_error_string", nativeLibJava.ep_get_err_details());
                            intent.putExtra("volume_sold", nativeLibJava.ep_get_vol_sold());
                            intent.putExtra("amount_sold", nativeLibJava.ep_get_amo_sold());
                            intent.putExtra("transaction_value", nativeLibJava.ep_get_value());
                            intent.putExtra("transaction_type", nativeLibJava.ep_get_value_ty());
                            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                            /*runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    String errorString = "";
                                    String transState = TransactionState.getString(nativeLibJava.ep_get_cur_state());
                                    String pumpState = PumpState.getString(nativeLibJava.ep_get_pump_state());
                                    if (nativeLibJava.ep_get_cur_state() == 8) {
                                        errorString = nativeLibJava.ep_get_err_details();
                                        transState = transState + errorString;
                                    }
                                    txtTransState.setText(transState);

                                    txtPumpState.setText(pumpState);
                                }
                            });*/
                        }

                        @Override
                        public void onFinish() {
                            start();
                        }
                    };
                    countDownTimer.start();

                    if (!runCalled) {
                        new Thread(new Ep_Run(nativeLibJava)).start();
                        runCalled = true;
                    }
                    socketConnection(ipAddress);
                }

                @Override
                public void onLosing(@NonNull Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    wifiAvailability = 2;
                    super.onLost(network);
                    nativeLibJava.ep_end_trans();
                    data_interface.initComplete(false);
                    /*runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnMessage.setEnabled(false);
                        }
                    });*/
                }

                @Override
                public void onUnavailable() {
                    wifiAvailability = 1;
                    super.onUnavailable();
                }
            };
            connectivityManager.requestNetwork(networkRequest, networkCallback);
        }
    }

    private void socketConnection(final String ip){
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //Replace below IP with the IP of that device in which server socket open.
                    //If you change port then change the port number in the server side code also.
                    socket = new Socket(ip, 5555);

                    OutputStream out = socket.getOutputStream();

                    output = new PrintWriter(out);

                    /*output.println(msg[0]);
                    output.flush();*/
                    final BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    do {
                        final String st = input.readLine();

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (st != null) {
                                    nativeLibJava.ep_rx_data(st, st.length());
                                } else {
                                    try {
                                        output.close();
                                        input.close();
                                        socket.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    } while (socket != null && socket.isConnected());

                    /*output.close();
                    out.close();
                    s.close();*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();
    }

    public int startTransaction(final TransactionType transactionType, final String pumpName,
                                final String tag, final double amount) {
        if (wifiAvailability == 0) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int yy = Calendar.getInstance().get(Calendar.YEAR);
                    int mon = Calendar.getInstance().get(Calendar.MONTH);
                    int dd = Calendar.getInstance().get(Calendar.DATE);
                    int hh = Calendar.getInstance().get(Calendar.HOUR);
                    int mm = Calendar.getInstance().get(Calendar.MINUTE);
                    int ss = Calendar.getInstance().get(Calendar.SECOND);
                    yy = yy - 2000;
                    int time = nativeLibJava.ep_get_time_int(ss, mm, hh, dd, mon, yy);

                    nativeLibJava.ep_start_trans(pumpName, transactionType.ordinal(), tag, (byte) ValueType.Amount.ordinal(), (float) amount, time, mTerminalId);
                }
            }).start();

            Intent intent = new Intent(mContext, TransactionActivity.class);
            ((Activity) mContext).startActivityForResult(intent, TRANSACTION_START);
        }
        return wifiAvailability;
    }

    public void continueTransaction(){
        Intent intent = new Intent(mContext, TransactionActivity.class);
        ((Activity) mContext).startActivityForResult(intent, TRANSACTION_START);
    }

    public void dispose() {
        if (!disposed) {
            disposed = true;
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
            nativeLibJava.ep_end_trans();

            try {
                socket.close();
                output.close();

                socket = null;
                output = null;
            } catch (IOException e) {
                e.printStackTrace();
            }

            turnWifi(false);
        }
    }
}
