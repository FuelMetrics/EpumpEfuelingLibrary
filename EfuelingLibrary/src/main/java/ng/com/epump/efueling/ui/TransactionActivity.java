package ng.com.epump.efueling.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import ng.com.epump.efueling.R;
import ng.com.epump.efueling.models.PumpState;
import ng.com.epump.efueling.models.TransactionState;

public class TransactionActivity extends AppCompatActivity {
    private Context context;
    private LinearLayout layoutTrans;
    private ProgressBar progressBar;
    private TextView txtPumpState, txtTransState, txtProgress, txtAuthorizedAmount, txtVolume, txtAmount;
    private Button button;
    private int pumpState, transactionState;
    private String stateString = "";
    private double amount = 0, volume = 0, transAmount = 0;
    private int percentage = 0;
    BroadcastReceiver infoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pumpState = intent.getIntExtra("pump_state", 0);
            transactionState = intent.getIntExtra("transaction_state", 0);
            stateString = intent.getStringExtra("transaction_state_string");
            if (transactionState == TransactionState.ST_PUMP_AUTH){
                transAmount = Double.parseDouble(Float.valueOf(intent.getFloatExtra("transaction_amount", 0f)).toString());
            }
            if (transactionState == TransactionState.ST_PUMP_FILLING || transactionState == TransactionState.ST_PUMP_FILL_COMP) {
                amount = Double.parseDouble(Float.valueOf(intent.getFloatExtra("amount_sold", 0f)).toString());
                volume = Double.parseDouble(Float.valueOf(intent.getFloatExtra("volume_sold", 0f)).toString());

                if (transAmount > 0 && amount > 0){
                    percentage = (int)((amount / transAmount) * 100);
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (transactionState == TransactionState.ST_PUMP_FILLING || transactionState == TransactionState.ST_PUMP_FILL_COMP) {
                        layoutTrans.setVisibility(View.VISIBLE);
                        txtAmount.setText(String.valueOf(amount));
                        txtVolume.setText(String.valueOf(txtVolume));
                        txtAuthorizedAmount.setText(String.valueOf(transAmount));
                    }
                    else {
                        layoutTrans.setVisibility(View.GONE);
                    }
                    Log.i("TAG", "run: pumpState " + pumpState);
                    Log.i("TAG", "run: transactionState " + transactionState);
                    Log.i("TAG", "run: errorString " + stateString);
                    Log.i("TAG", "run: amount " + amount);
                    Log.i("TAG", "run: volume " + volume);
                    String transState = TransactionState.getString(transactionState);
                    String pState = PumpState.getString(pumpState);
                    if (pumpState == 8) {
                        transState = transState + stateString;
                    }
                    txtTransState.setText(transState);

                    txtPumpState.setText(pState);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar.setProgress(percentage, true);
                    }
                    else{
                        progressBar.setProgress(percentage);
                    }

                    txtProgress.setText(String.format("%d%%", percentage));
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        context = this;

        layoutTrans = findViewById(R.id.layoutTrans);
        txtPumpState = findViewById(R.id.txtPumpState);
        txtTransState = findViewById(R.id.txtTransState);
        progressBar = findViewById(R.id.progressBar);
        txtProgress = findViewById(R.id.txtProgress);
        txtAuthorizedAmount = findViewById(R.id.txtAuthorizedAmount);
        txtVolume = findViewById(R.id.txtVolume);
        txtAmount = findViewById(R.id.txtAmount);
        button = findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        LocalBroadcastManager.getInstance(context)
                .registerReceiver(infoReceiver,
                        new IntentFilter("get_States"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(context)
                .unregisterReceiver(infoReceiver);
    }
}