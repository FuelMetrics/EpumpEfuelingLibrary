package ng.com.epump.epumpconnection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiNetworkSpecifier;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.fuelmetrics.epumpwifitool.NativeLibJava;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class MainActivity extends AppCompatActivity implements IData {
    Switch wifiSwitch;
    Button connectBtn, btnMessage;
    TextView txtPumpState, txtTransState;
    Context context;
    Activity activity;
    WifiManager wifiManager;
    NativeLibJava nativeLibJava;
    PrintWriter output;
    Socket s;
    boolean runCalled = false;
    NumUtil nu;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = activity = this;
        wifiSwitch = findViewById(R.id.wifiSwitch);
        connectBtn = findViewById(R.id.connectBtn);
        btnMessage = findViewById(R.id.btnMessage);
        txtPumpState = findViewById(R.id.txtPumpState);
        txtTransState = findViewById(R.id.txtTransState);
        App app = (App) getApplication();
        app.SetupInterface(this);
        nativeLibJava = ((App)getApplication()).nativeLibJava;

        nu = new NumUtil();

        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean state) {
                turnWifi(state);
                connectBtn.setEnabled(state);
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                connect2Wifi("Remis-2", "P@55w0rdL3n914");
            }
        });

        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String uid = String.valueOf(nu.genNum());
                nativeLibJava.ep_start_trans("P1", 0, uid, 0.1f);
            }
        });

        /*IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        context.registerReceiver(new NetworkStateReceiver(), intentFilter);*/
    }

    private void turnWifi(boolean state){
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(WIFI_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Intent panelIntent = new Intent(Settings.Panel.ACTION_WIFI);
            activity.startActivityForResult(panelIntent, 223);
        } else {
            wifiManager.setWifiEnabled(state);
        }
    }

    private void connect2Wifi(String yourSsid, String password){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            try {
                WifiConfiguration wifiConfig = new WifiConfiguration();
                wifiConfig.SSID = "\"" + yourSsid + "\"";
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
                    .setSsid(yourSsid)
                    .setWpa2Passphrase(password)
                    .build();

            NetworkRequest networkRequest = new NetworkRequest.Builder()
                    .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                    .setNetworkSpecifier(wifiNetworkSpecifier)
                    .build();

            final ConnectivityManager connectivityManager = (ConnectivityManager)context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback(){
                @Override
                public void onAvailable(@NonNull Network network) {
                    super.onAvailable(network);
                    connectivityManager.bindProcessToNetwork(network);
                    nativeLibJava.registerCallbacks();
                    int res = nativeLibJava.ep_init();
                    if (res == 0){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                btnMessage.setEnabled(true);
                            }
                        });
                    }

                    CountDownTimer ct = new CountDownTimer(60000, 100) {
                        @Override
                        public void onTick(long l) {
                            nativeLibJava.ep_ms_timer();
                            runOnUiThread(new Runnable() {
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
                            });
                        }

                        @Override
                        public void onFinish() {
                            start();
                        }
                    };
                    ct.start();

                    if (!runCalled) {
                        new Thread(new Ep_Run(nativeLibJava)).start();
                        runCalled = true;
                    }
                    socketConnection("192.168.4.2");
                }

                @Override
                public void onLosing(@NonNull Network network, int maxMsToLive) {
                    super.onLosing(network, maxMsToLive);
                }

                @Override
                public void onLost(@NonNull Network network) {
                    super.onLost(network);
                    nativeLibJava.ep_end_trans();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            btnMessage.setEnabled(false);
                        }
                    });
                }

                @Override
                public void onUnavailable() {
                    super.onUnavailable();
                }
            };
            connectivityManager.requestNetwork(networkRequest, networkCallback);
        }
    }

    private void socketConnection(final String ip, final String... msg){
        final Handler handler = new Handler();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //Replace below IP with the IP of that device in which server socket open.
                    //If you change port then change the port number in the server side code also.
                    s = new Socket(ip, 5555);

                    OutputStream out = s.getOutputStream();

                    output = new PrintWriter(out);

                    /*output.println(msg[0]);
                    output.flush();*/
                    final BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
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
                                        s.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                Log.i("TAG from server", "run: " + st);
                            }
                        });
                    } while (s.isConnected());

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 223 && resultCode == 0){

        }
    }

    @Override
    public void tx_data(String data, int len) {
        Log.i("TAG", "tx_data: " + data);

        if (output != null){
            output.println(data);
            output.flush();
        }
    }

    public class NetworkStateReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            Log.d("app","Network connectivity change");

            if(intent.getExtras() != null)
            {
                NetworkInfo ni = (NetworkInfo) intent.getExtras().get(ConnectivityManager.EXTRA_NETWORK_INFO);
                if(ni != null && ni.getState() == NetworkInfo.State.CONNECTED)
                {
                    Log.i("app", "Network " + ni.getTypeName() + " connected");
                }
            }

            if(intent.getExtras().getBoolean(ConnectivityManager.EXTRA_NO_CONNECTIVITY, Boolean.FALSE))
            {
                Log.d("app", "There's no network connectivity");
            }
        }
    }
}


class NumUtil {
    private Set<Integer> s = new HashSet<>();
    private Random r = new Random();
    private int maxNum = 99999999;

    // Randomly generate an 8-digit number
    public int genNum() {
        // run out of numbers
        if (s.size() >= 89999999) {
            return -1;
        }

        // Randomly generate 8 digits
        int n = r.nextInt(89999999) + 10000000;
        while (!s.add(n)) {
            n++;

            if (n > maxNum) {
                n = 0;
            }
        }
        return n;
    }

    // Reclaim a number
    public void reUse(int num) {
        s.remove(num);
    }
}
