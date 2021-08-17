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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import ng.com.epump.efueling.R;
import ng.com.epump.efueling.models.Error;
import ng.com.epump.efueling.models.PumpState;
import ng.com.epump.efueling.models.TransactionState;
import ng.com.epump.efueling.models.Utility;
import ng.com.epump.efueling.models.ValueType;

public class TransactionActivity extends AppCompatActivity {
    private Context context;
    private LinearLayout layoutTrans;
    private ProgressBar progressBar;
    private TextView txtTransState, txtProgress, txtAuthorizedAmount, txtVolume,
            txtAmount, txtValueType;
    private Button btnEndTrans;
    private ImageView imgDismiss;
    private int pumpState, transactionState;
    private String errorString = "", sessionId = "";
    private double amount = 0, volume = 0, transValue = 0;
    private int transType;
    private int percentage = 0;
    private int return_value;
    private boolean transComplete, errorOccurred;
    BroadcastReceiver infoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pumpState = intent.getIntExtra("pump_state", 0);
            transactionState = intent.getIntExtra("transaction_state", 0);
            errorString = intent.getStringExtra("transaction_error_string");
            if (transactionState == TransactionState.ST_PUMP_AUTH || transactionState == TransactionState.ST_PUMP_FILLING){
                transValue = Double.parseDouble(Float.valueOf(intent.getFloatExtra("transaction_value", 0f)).toString());
                transType = intent.getByteExtra("transaction_type", (byte) 0x00);
                sessionId = intent.getStringExtra("transaction_session_id");

            }
            if (transactionState == TransactionState.ST_PUMP_FILLING || transactionState == TransactionState.ST_PUMP_FILL_COMP) {
                amount = Double.parseDouble(Float.valueOf(intent.getFloatExtra("amount_sold", 0f)).toString());
                volume = Double.parseDouble(Float.valueOf(intent.getFloatExtra("volume_sold", 0f)).toString());

                if (transType == ValueType.Amount.ordinal()) {
                    txtValueType.setText("Amount Authorized");
                    if (amount >= transValue){
                        transComplete = true;
                    }
                    if (transValue > 0 && amount > 0) {
                        percentage = (int) ((amount / transValue) * 100);
                    }
                }
                else if (transType == ValueType.Volume.ordinal()) {
                    txtValueType.setText("Volume Authorized");
                    if (volume >= transValue){
                        transComplete = true;
                    }
                    if (transValue > 0 && volume > 0) {
                        percentage = (int) ((volume / transValue) * 100);
                    }
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (transactionState == TransactionState.ST_PUMP_AUTH || transactionState == TransactionState.ST_PUMP_FILLING || transactionState == TransactionState.ST_PUMP_FILL_COMP) {
                        layoutTrans.setVisibility(View.VISIBLE);
                        txtAmount.setText(Utility.convert2DecimalString(amount, false));
                        txtVolume.setText(String.format("%s L", Utility.convert2DecimalString(volume, false)));
                        txtAuthorizedAmount.setText(Utility.convert2DecimalString(transValue, false));
                    }
                    else {
                        layoutTrans.setVisibility(View.GONE);
                    }
                    Log.i("TAG", "run: pumpState " + pumpState);
                    Log.i("TAG", "run: transactionState " + transactionState);
                    Log.i("TAG", "run: errorString " + errorString);
                    Log.i("TAG", "run: amount " + amount);
                    Log.i("TAG", "run: volume " + volume);
                    String transState = TransactionState.getString(transactionState);
                    String pState = PumpState.getString(pumpState);
                    if (pumpState == TransactionState.ST_ERROR || transactionState == TransactionState.ST_ERROR ||
                            transactionState == TransactionState.ST_PUMP_BUSY) {
                        if (transactionState == TransactionState.ST_PUMP_BUSY){
                            transState = pState;
                        }
                        else {
                            transState = Error.getError(errorString);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            txtTransState.setTextColor(getColor(R.color.libraryColorRed));
                        }
                        else{
                            txtTransState.setTextColor(getResources().getColor(R.color.libraryColorRed));
                        }
                        errorOccurred = true;
                    }
                    else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            txtTransState.setTextColor(getColor(R.color.libraryColorPrimary));
                        }
                        else{
                            txtTransState.setTextColor(getResources().getColor(R.color.libraryColorPrimary));
                        }
                        errorOccurred = false;
                    }

                    if (transactionState == TransactionState.ST_PUMP_AUTH &&
                            (pumpState == PumpState.NOZZLE_HANG_UP || pumpState == PumpState.PUMP_AUTH_NOZZLE_HANG_UP)){
                        transState = PumpState.getString(pumpState);
                    }
                    txtTransState.setText(transState);

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

        setTheme(R.style.Library_Theme);
        context = this;

        layoutTrans = findViewById(R.id.layoutTrans);
        txtTransState = findViewById(R.id.txtTransState);
        progressBar = findViewById(R.id.progressBar);
        txtProgress = findViewById(R.id.txtProgress);
        txtAuthorizedAmount = findViewById(R.id.txtAuthorizedAmount);
        txtVolume = findViewById(R.id.txtVolume);
        txtAmount = findViewById(R.id.txtAmount);
        txtValueType = findViewById(R.id.txtValueType);
        btnEndTrans = findViewById(R.id.btnEndTrans);
        imgDismiss = findViewById(R.id.imgDismiss);

        btnEndTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnData = new Intent();
                returnData.putExtra("sessionId", sessionId);
                returnData.putExtra("volume", volume);
                returnData.putExtra("amount", amount);
                returnData.putExtra("transactionValue", transValue);
                return_value = -1;
                setResult(return_value, returnData);
                finish();
            }
        });

        imgDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnData = new Intent();
                returnData.putExtra("sessionId", sessionId);
                returnData.putExtra("volume", volume);
                returnData.putExtra("amount", amount);
                returnData.putExtra("transactionValue", transValue);
                if (transComplete || errorOccurred){
                    return_value = -1;
                }
                else {
                    return_value = 0;
                }
                setResult(return_value, returnData);
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