package ng.com.epump.epumpconnection;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import ng.com.epump.efueling.EfuelingConnect;
import ng.com.epump.efueling.interfaces.IData;
import ng.com.epump.efueling.models.TransactionType;
import ng.com.epump.efueling.models.Utility;

import static ng.com.epump.efueling.EfuelingConnect.TRANSACTION_START;

public class MainActivity extends AppCompatActivity implements IData {
    Switch wifiSwitch;
    Button connectBtn, btnMessage;
    Spinner spinner;
    EditText txtTag;
    Context context;
    Activity activity;
    NumUtil nu;
    EfuelingConnect efuelingConnect;
    int transactionType, mResultCode = -1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = activity = this;
        wifiSwitch = findViewById(R.id.wifiSwitch);
        connectBtn = findViewById(R.id.connectBtn);
        btnMessage = findViewById(R.id.btnMessage);
        spinner = findViewById(R.id.spinner);
        txtTag = findViewById(R.id.txtTag);

        nu = new NumUtil();

        wifiSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean state) {
                if (state){
                    efuelingConnect.init("");
                }
                efuelingConnect.turnWifi(state);
                connectBtn.setEnabled(state);
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                efuelingConnect.connect2WifiAndSocket("Remis-2", "P@55w0rdL3n914", "192.168.4.2");
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                transactionType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mResultCode > -1){
                    efuelingConnect.continueTransaction();
                }
                else{
                    String tag = txtTag.getText().toString();
                    double amount = 10;
                    if (!tag.isEmpty()){
                        RandomGenerator rdg = new RandomGenerator(8, new SecureRandom());
                        String uniqueTg = rdg.nextString();
                        TransactionType transactionType1 = null;
                        if (transactionType == TransactionType.Voucher.ordinal()){
                            transactionType1 = TransactionType.Voucher;
                        }
                        else if (transactionType == TransactionType.Remis.ordinal()){
                            tag = tag + "|" + uniqueTg;
                            transactionType1 = TransactionType.Remis;
                        }
                        else if (transactionType == TransactionType.Card.ordinal()){
                            String pin = Utility.padPassword("1234");
                            tag = tag + "|" + pin + "|" + amount + "|" + uniqueTg;
                            transactionType1 = TransactionType.Card;
                        }
                        else if (transactionType == TransactionType.POS.ordinal()){
                            transactionType1 = TransactionType.POS;
                        }
                        else if (transactionType == TransactionType.Offline_Voucher.ordinal()){
                            transactionType1 = TransactionType.Offline_Voucher;
                        }
                        else if (transactionType == TransactionType.Offline_Remis.ordinal()){
                            tag = tag + "|" + uniqueTg;
                            transactionType1 = TransactionType.Offline_Remis;
                        }
                        else if (transactionType == TransactionType.Offline_Card.ordinal()){
                            String pin = Utility.padPassword("1234");
                            tag = tag + "|" + pin + "|" + amount + "|" + uniqueTg;
                            transactionType1 = TransactionType.Offline_Card;
                        }
                        else if (transactionType == TransactionType.Offline_POS.ordinal()){
                            transactionType1 = TransactionType.Offline_POS;
                        }
                        int done = efuelingConnect.startTransaction(transactionType1, "P1", tag, amount);
                        if (done > 0){
                            Toast.makeText(context, "WIFI not available", Toast.LENGTH_LONG).show();
                        }
                    }
                    else{
                        Toast.makeText(context, "Input tag", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        efuelingConnect = EfuelingConnect.getInstance(context);

        IntentFilter intentFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
        context.registerReceiver(new NetworkStateReceiver(), intentFilter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == TRANSACTION_START){
            mResultCode = resultCode;
            if (resultCode == -1) {
                if (efuelingConnect != null) {
                    efuelingConnect.dispose();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (efuelingConnect != null) {
            efuelingConnect.dispose();
        }
    }

    @Override
    public void initComplete(final boolean complete) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                btnMessage.setEnabled(complete);
            }
        });
    }

    public static class NetworkStateReceiver extends BroadcastReceiver
    {
        public void onReceive(Context context, Intent intent)
        {
            Log.d("app","Network connectivity change");

            ConnectivityManager conMan = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = conMan.getActiveNetworkInfo();

            if (netInfo != null && netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                //wifiSwitch.setChecked(true);
                Log.d("WifiReceiver", "Have Wifi Connection");
            }
            else {
                //wifiSwitch.setChecked(false);
                Log.d("WifiReceiver", "Don't have Wifi Connection");
            }

            /*if(intent.getExtras() != null)
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
            }*/
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
