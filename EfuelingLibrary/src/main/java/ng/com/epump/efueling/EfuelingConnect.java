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
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fuelmetrics.epumpwifitool.JNICallbackInterface;
import com.fuelmetrics.epumpwifitool.NativeLibJava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ng.com.epump.efueling.interfaces.BluetoothUtilsCallback;
import ng.com.epump.efueling.interfaces.IData;
import ng.com.epump.efueling.interfaces.TransactionCallback;
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
    private ConnectivityManager connectivityManager;
    private ConnectivityManager.NetworkCallback networkCallback;
    private BluetoothUtils mBluetoothUtils;
    private static boolean runCalled;
    private PrintWriter output;
    private Socket socket;
    private CountDownTimer countDownTimer, messageCountDownTimer;
    private int wifiAvailability = -1;
    private boolean disposed = false;
    private Activity activity;
    private String mDailyKey = "";
    private String mTerminalId = "";
    private Date transactionDate;
    private int connectionTrial = 0;
    private Thread thread, epRun;
    private Handler handler;

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
        /*data_interface = (IData) mContext;*/
    }

    public void init(String dailyKey) {
        init(dailyKey, "");
    }

    @Override
    public void tx_data(String data, int len) {
        if(mBluetoothUtils != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                mBluetoothUtils.write(data);
            }
        }
        else if (output != null) {
            output.println(data);
            output.flush();
        }

        /*Intent intent = new Intent("wifi_state");
        intent.putExtra("pump_state", nativeLibJava.ep_get_pump_state());
        intent.putExtra("transaction_state", nativeLibJava.ep_get_cur_state());
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
        data_interface.tx_data(data, len);*/
    }

    /*WiFi methods*/
    public void turnWifi(final boolean state) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                //if (!state) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        List<WifiConfiguration> list = wifiManager.getConfiguredNetworks();
                        if (list != null){
                            for (WifiConfiguration i : list) {
                                wifiManager.disableNetwork(i.networkId);
                                wifiManager.removeNetwork(i.networkId);
                            }
                        }
                        wifiManager.saveConfiguration();
                    }
                //}
                if (wifiManager.isWifiEnabled() != state) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
                        ((Activity)mContext).startActivityForResult(panelIntent, 223);
                    } else {
                        wifiManager.setWifiEnabled(false);
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

    public void connect2WifiAndSocket(final String ssid, String password, final String ipAddress){
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
                connectivityManager = (ConnectivityManager)mContext.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
                networkCallback = new ConnectivityManager.NetworkCallback(){
                    @Override
                    public void onAvailable(@NonNull Network network) {
                        super.onAvailable(network);
                        WifiInfo info = wifiManager.getConnectionInfo();
                        String connectedSSID  = info.getSSID();
                        if (connectedSSID != null){
                            connectedSSID = connectedSSID.replaceAll("\"", "");
                        }
                        if (Objects.equals(connectedSSID, ssid)){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                connectivityManager.bindProcessToNetwork(network);
                            }
                            if (!Utility.ConnectionStarted) {
                                Utility.ConnectionStarted = true;
                                handleConnect(ipAddress);
                            }
                        }
                        else {
                            Utility.ConnectionStarted = false;
                            Log.i("TAG", "onAvailable: wrong ssid connection");
                        }
                    }

                    @Override
                    public void onLosing(@NonNull Network network, int maxMsToLive) {
                        super.onLosing(network, maxMsToLive);
                        Utility.ConnectionStarted = false;
                    }

                    @Override
                    public void onLost(@NonNull Network network) {
                        if (!disposed){
                            wifiAvailability = 2;
                            super.onLost(network);
                            Utility.ConnectionStarted = false;

                            Intent intent = new Intent("init_complete");
                            intent.putExtra("status", false);
                            LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
                            /*if (data_interface != null){
                                data_interface.initComplete(false);
                            }*/
                        }
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

    private void handleConnect(String... ipAddress){
        handler = new Handler(Looper.getMainLooper());
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
                LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
                //LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
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
        if (ipAddress.length > 0){
            socketConnection(ipAddress[0]);
        }
    }

    private void socketConnection(final String ip){
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    socket = new Socket(ip, 5555);
                    /*socket.setKeepAlive(true);*/
                    OutputStream out = socket.getOutputStream();

                    output = new PrintWriter(out);

                    do{
                        Log.d("TAG", "run: connecting");
                    }
                    while (!socket.isBound() && !socket.isConnected());
                    /*if (data_interface != null){
                        data_interface.initComplete(true);
                    }*/
                    Intent intent = new Intent("init_complete");
                    intent.putExtra("status", true);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);

                    while (socket != null && socket.isBound() && socket.isConnected() && !socket.isClosed()) {
                        final BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        final String st = input.readLine();

                        pushDataToLib(st);
                    }

                    if (socket != null && socket.isClosed()) {
                        if (connectionTrial < 3){
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
    /*WiFi methods ended*/

    private void pushDataToLib(String dataToSend) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (dataToSend != null) {
                    nativeLibJava.ep_rx_data(dataToSend, dataToSend.length());
                }
            }
        });
    }
    /*BLE methods*/

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public boolean initBluetooth(String macAddress){
        mBluetoothUtils = new BluetoothUtils(mContext, macAddress, new BluetoothUtilsCallback() {
            @Override
            public void onConnected() {
                handleConnect();
            }

            @Override
            public void onRead(String data) {
                pushDataToLib(data);
            }
        });
        return mBluetoothUtils.bLESupported();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void startBLE(){
        mBluetoothUtils.startBLE();
    }

    /*To be called on Stop*/
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void closeGatt(){
        mBluetoothUtils.closeGatt();
    }
    /*BLE methods ended*/

    public void startTransaction(final TransactionType transactionType, final String pumpName, final String pumpDisplayName,
                                 final String tag, final double amount, TransactionCallback callback) {
        if (wifiAvailability <= 0) {
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
            intent.putExtra("Pump_Display_Name", pumpDisplayName);
            ((Activity) mContext).startActivityForResult(intent, TRANSACTION_START);
            try{
                TransactionActivity.setCallback(callback);
            }
            catch (Exception e){

            }
        }
    }

    public void continueTransaction(){
        Intent intent = new Intent(mContext, TransactionActivity.class);
        ((Activity) mContext).startActivityForResult(intent, TRANSACTION_START);
    }

    public void dispose() {
        if (!disposed) {
            disposed = true;
            Utility.ConnectionStarted = false;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    if (networkCallback != null && connectivityManager != null) {
                        connectivityManager.unregisterNetworkCallback(networkCallback);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
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

            if (mBluetoothUtils != null){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                    mBluetoothUtils.closeGatt();
                }
                mBluetoothUtils = null;
            }
            else {
                turnWifi(false);
            }
            _connect = null;
            //data_interface = null;
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

    public ArrayList<Transaction> readTransactions(int count){
        ArrayList<Transaction> myTransactions = new ArrayList<>();
        int counter = 1;
        while (nativeLibJava.ep_get_transaction(counter) == 0 || counter <= count){
            byte transType = nativeLibJava.ep_read_trans_ty();
            String transId = nativeLibJava.ep_read_trans_uid();
            double transValue = nativeLibJava.ep_read_trans_value();
            byte transValueType = nativeLibJava.ep_read_trans_value_ty();

            String transactionType = TransactionType.get(transType);
            String transactionValueType = TransactionValueType.get(transValueType);
            myTransactions.add(new Transaction(transactionType, transId, transValue, transactionValueType));
            counter++;
        }
        return myTransactions;
    }
}
