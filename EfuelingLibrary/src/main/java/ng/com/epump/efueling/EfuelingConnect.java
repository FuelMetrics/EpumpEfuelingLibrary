package ng.com.epump.efueling;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fuelmetrics.epumpwifitool.NativeLibJava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import ng.com.epump.efueling.interfaces.IData;

import com.fuelmetrics.epumpwifitool.JNICallbackInterface;

import ng.com.epump.efueling.models.Ep_Run;
import ng.com.epump.efueling.models.Transaction;
import ng.com.epump.efueling.models.TransactionType;
import ng.com.epump.efueling.models.TransactionValueType;
import ng.com.epump.efueling.models.Utility;
import ng.com.epump.efueling.ui.NFCActivity;
import ng.com.epump.efueling.ui.TransactionActivity;

public class EfuelingConnect implements JNICallbackInterface {
    private static final int GA_NFC_REQUEST_CODE = 0011;
    private static final int EP_NFC_REQUEST_CODE = 0012;
    public static final int TRANSACTION_START = 213;
    public static final int EP_SETTINGS_REQUEST_CODE = 0100;
    public NativeLibJava nativeLibJava;
    @SuppressLint("StaticFieldLeak")
    private static EfuelingConnect _connect;
    private Context mContext;
    private IData data_interface;
    private WifiManager wifiManager;
    private static boolean runCalled;
    private PrintWriter output;
    private Socket socket;
    private CountDownTimer countDownTimer, messageCountDownTimer;
    private int wifiAvailability = 1;
    private boolean disposed;
    private Activity activity;
    private String mDailyKey = "";
    private String mTerminalId = "";
    private Date transactionDate;
    private int connectionTrial = 0;
    private Thread thread, epRun;
    private ExecutorService executor;
    private Future epRunFuture, socketFuture;

    private EfuelingConnect(Context context) {
        this.mContext = context;
        if (context instanceof Activity) {
            this.activity = (Activity) context;
        }
    }

    public static EfuelingConnect getInstance(Context context) {
        /*if (_connect == null) {
            _connect = new EfuelingConnect(context);
        }
        return _connect;*/
        return new EfuelingConnect(context);
    }

    public void init(String dailyKey, String terminalId) {
        mDailyKey = dailyKey;
        if (!terminalId.isEmpty()) {
            mTerminalId = terminalId;
        }
        if (disposed) {
            disposed = false;
        }
        /*if (nativeLibJava != null){
            nativeLibJava.ep_end_trans();
        }*/
        nativeLibJava = new NativeLibJava(this);
        data_interface = (IData) mContext;
    }

    public void init(String dailyKey) {
        init(dailyKey, "");
    }

    @Override
    public void tx_data(String data, int len) {
        if (output != null) {
            output.println(data);
            output.flush();
        }

        /*Intent intent = new Intent("wifi_state");
        intent.putExtra("pump_state", nativeLibJava.ep_get_pump_state());
        intent.putExtra("transaction_state", nativeLibJava.ep_get_cur_state());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        data_interface.tx_data(data, len);*/
    }

    public void turnWifi(final boolean state) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                if (!state) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    else {
                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                        if (list != null){
                            for (WifiConfiguration i : list) {
                                wifiManager.disableNetwork(i.networkId);
                                wifiManager.removeNetwork(i.networkId);
                            }
                        }
                    }
                }
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

    public boolean wifiEnabled(){
        wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }

    public void connect2WifiAndSocket(String ssid, String password, final String ipAddress){
        do{
            Log.i("TAG", "run: switching wifi on");
        }
        while (!wifiEnabled());
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
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

                handleConnect(ipAddress);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            NetworkRequest networkRequest;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                WifiNetworkSpecifier wifiNetworkSpecifier = new WifiNetworkSpecifier.Builder()
                        .setSsid(ssid)
                        .setWpa2Passphrase(password)
                        .build();

                networkRequest = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                        .setNetworkSpecifier(wifiNetworkSpecifier).build();
            }
            else {
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = "\"" + ssid + "\"";
                wifiConfig.preSharedKey = "\"" + password + "\"";
                int netId = wifiManager.addNetwork(wifiConfig);
                wifiManager.disconnect();
                wifiManager.enableNetwork(netId, true);
                wifiManager.reconnect();
                networkRequest = new NetworkRequest.Builder()
                        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI).build();
            }
            boolean cnt = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(mContext)){
                    cnt = false;
                    Intent goToSettings = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    goToSettings.setData(Uri.parse("package:" + mContext.getPackageName()));
                    ((Activity) mContext).startActivityForResult(goToSettings, EP_SETTINGS_REQUEST_CODE);
                }
            }
            if (cnt){
                final ConnectivityManager connectivityManager = (ConnectivityManager)mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(){
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        super.onAvailable(network);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            connectivityManager.bindProcessToNetwork(network);
                        }
                        if (!Utility.ConnectionStarted) {
                            Utility.ConnectionStarted = true;
                            handleConnect(ipAddress);
                        }
                    }

                    @Override
                    public void onLosing(@NonNull Network network, int maxMsToLive) {
                        super.onLosing(network, maxMsToLive);
                        Utility.ConnectionStarted = false;
                    }

                    @Override
                    public void onLost(@NonNull Network network) {
                        wifiAvailability = 2;
                        super.onLost(network);
                        Utility.ConnectionStarted = false;
                        data_interface.initComplete(false);
                    }

                    @Override
                    public void onUnavailable() {
                        wifiAvailability = 1;
                        super.onUnavailable();
                        Utility.ConnectionStarted = false;
                    }
                };
                connectivityManager.requestNetwork(networkRequest, networkCallback);
            }
        }
    }

    private void handleConnect(String ipAddress){
        wifiAvailability = 0;

        nativeLibJava.registerCallbacks();
        nativeLibJava.ep_init("", mDailyKey);

        countDownTimer = new CountDownTimer(60000, 100) {
            @Override
            public void onTick(long l) {
                nativeLibJava.ep_ms_timer();
            }

            @Override
            public void onFinish() {
                start();
            }
        };
        countDownTimer.start();

        messageCountDownTimer = new CountDownTimer(60000, 500) {
            @Override
            public void onTick(long l) {
                int pumpState = nativeLibJava.ep_get_pump_state();
                int transState = nativeLibJava.ep_get_cur_state();
                String transError = nativeLibJava.ep_get_err_details();
                float volume = nativeLibJava.ep_get_vol_sold();
                float amount = nativeLibJava.ep_get_amo_sold();
                float transValue = nativeLibJava.ep_get_value();
                byte transType = nativeLibJava.ep_get_value_ty();
                String transSessionId = nativeLibJava.ep_get_session_id();

                Intent intent = new Intent("get_States");
                intent.putExtra("pump_state", pumpState);
                intent.putExtra("transaction_state", transState);
                intent.putExtra("transaction_error_string", transError);
                intent.putExtra("volume_sold", volume);
                intent.putExtra("amount_sold", amount);
                intent.putExtra("transaction_value", transValue);
                intent.putExtra("transaction_type", transType);
                intent.putExtra("transaction_session_id", transSessionId);
                //LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
                LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
            }

            @Override
            public void onFinish() {
                start();
            }
        };
        messageCountDownTimer.start();

        if (!runCalled) {
            runCalled = true;
            /*if (executor == null || executor.isShutdown()){
                executor = Executors.newSingleThreadExecutor();
            }
            epRunFuture =  executor.submit(new Ep_Run(nativeLibJava));*/
            epRun = new Thread(new Ep_Run(nativeLibJava));
            epRun.start();
        }
        socketConnection(ipAddress);
    }

    private void socketConnection(final String ip){
        final Handler handler = new Handler(Looper.getMainLooper());
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    socket = new Socket(ip, 5555);
                    socket.setKeepAlive(true);
                    OutputStream out = socket.getOutputStream();

                    output = new PrintWriter(out);

                    do{
                        Log.d("TAG", "run: connecting");
                    }
                    while (!socket.isBound() && !socket.isConnected());
                    data_interface.initComplete(true);

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

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });
                    } while (socket != null && socket.isBound() && socket.isConnected() && !socket.isClosed());

                    if (socket != null && socket.isClosed()) {
                        if (!disposed && connectionTrial < 5){
                            connectionTrial++;
                            socketConnection(ip);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        /*if (executor == null || executor.isShutdown()){
            executor = Executors.newSingleThreadExecutor();
        }
        socketFuture = executor.submit(runnable);*/
        thread = new Thread(runnable);
        thread.start();
    }

    public int startTransaction(final TransactionType transactionType, final String pumpName,
                                final String tag, final double amount) {
        if (wifiAvailability == 0) {
            final Calendar calendar = Calendar.getInstance();
            transactionDate = calendar.getTime();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    int yy = calendar.get(Calendar.YEAR);
                    int mon = calendar.get(Calendar.MONTH);
                    int dd = calendar.get(Calendar.DATE);
                    int hh = calendar.get(Calendar.HOUR);
                    int mm = calendar.get(Calendar.MINUTE);
                    int ss = calendar.get(Calendar.SECOND);
                    yy = yy - 2000;
                    int time = nativeLibJava.ep_get_time_int(ss, mm, hh, dd, mon, yy);

                    nativeLibJava.ep_start_trans(pumpName, transactionType.ordinal(), tag, (byte) TransactionValueType.Amount.ordinal(), (float) amount, time, mTerminalId);
                }
            }).start();

            Intent intent = new Intent(mContext, TransactionActivity.class);
            intent.putExtra("Transaction_Date", transactionDate.getTime());
            intent.putExtra("Pump_Name", pumpName);
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
            try{
                if (thread != null) {
                    thread.interrupt();
                }
                if (epRun != null) {
                    epRun.interrupt();
                }

                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }

                if (messageCountDownTimer != null) {
                    messageCountDownTimer.cancel();
                    messageCountDownTimer = null;
                }
                if (nativeLibJava != null){
                    //nativeLibJava.ep_end_trans();
                    nativeLibJava.ep_deinit();
                }

                /*if (socketFuture != null && !socketFuture.isCancelled()){
                    socketFuture.cancel(true);
                }
                if (epRunFuture != null && !epRunFuture.isCancelled()){
                    epRunFuture.cancel(true);
                }
                if(executor != null && !executor.isShutdown()){
                    executor.shutdown();
                }
                executor = null;*/
                runCalled = false;
            }
            catch (Exception ex){
                ex.printStackTrace();
            }

            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                    output.close();
                }

                socket = null;
                output = null;
            } catch (IOException e) {
                e.printStackTrace();
            }

            turnWifi(false);
            _connect = null;
        }
    }

    public void stopTransaction(){
        if (nativeLibJava != null){
            nativeLibJava.ep_end_trans();
        }
    }

    public void readNFC() {
        Intent intent = new Intent(activity, NFCActivity.class);
        intent.setFlags(0);
        try {
            activity.startActivityForResult(intent, EP_NFC_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "Activity Not Found", Toast.LENGTH_LONG).show();
        }
    }

    public void readGANFC() {
        Intent intent = new Intent("com.globalaccelerex.read_nfc");
        intent.setFlags(0);
        try {
            activity.startActivityForResult(intent, GA_NFC_REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(mContext, "Activity Not Found", Toast.LENGTH_LONG).show();
        }
    }

    public void readTransactions(int count){
        int counter = 1;
        while (nativeLibJava.ep_get_transaction(counter) == 0 || counter <= count){
            byte transType = nativeLibJava.ep_read_trans_ty();
            String transId = nativeLibJava.ep_read_trans_uid();
            double transValue = nativeLibJava.ep_read_trans_value();
            byte transValueType = nativeLibJava.ep_read_trans_value_ty();

            String transactionType = TransactionType.get(transType);
            String transactionValueType = TransactionValueType.get(transValueType);
            new Transaction(transactionType, transId, transValue, transactionValueType);
            counter++;
        }
    }
}
