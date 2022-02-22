package ng.com.epump.efueling.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import ng.com.epump.efueling.R;
import ng.com.epump.efueling.interfaces.TransactionCallback;
import ng.com.epump.efueling.models.Error;
import ng.com.epump.efueling.models.PumpState;
import ng.com.epump.efueling.models.TransactionState;
import ng.com.epump.efueling.models.Utility;
import ng.com.epump.efueling.models.TransactionValueType;

public class TransactionActivity extends AppCompatActivity {
    private Context context;
    private ConstraintLayout constraintLayout;
    private LinearLayout layoutTrans;
    private ProgressBar progressBar;
    private TextView txtTransState, txtProgress, txtAuthorizedAmount, txtVolume,
            txtAmount, txtValueType;
    private Button btnEndTrans;
    private ImageView imgDismiss, img_conn_mode;
    private int pumpState, transactionState;
    private String errorString = "", sessionId = "", pumpName = "", pumpDisplayName = "",
            vouchercardNumber, connectionMode;
    private double amount = 0, volume = 0, transValue = 0;
    private int transType, transactionAck;
    private int percentage = 0;
    private int return_value;
    private boolean transComplete, errorOccurred, transactionStarted;
    private long transactionDate;
    private static TransactionCallback mCallback;
    Intent returnData;
    BroadcastReceiver infoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            pumpState = intent.getIntExtra("pump_state", 0);
            transactionState = intent.getIntExtra("transaction_state", 0);
            errorString = intent.getStringExtra("transaction_error_string");
            transactionAck = intent.getIntExtra("transaction_acknowledged", 0);
            if (transactionState == TransactionState.ST_PUMP_AUTH || transactionState == TransactionState.ST_PUMP_FILLING){
                transValue = Double.parseDouble(Float.valueOf(intent.getFloatExtra("transaction_value", 0f)).toString());
                transType = intent.getByteExtra("transaction_type", (byte) 0x00);
                sessionId = intent.getStringExtra("transaction_session_id");
            }
            if (transactionState == TransactionState.ST_PUMP_FILLING || transactionState == TransactionState.ST_PUMP_FILL_COMP) {
                amount = Double.parseDouble(Float.valueOf(intent.getFloatExtra("amount_sold", 0f)).toString());
                volume = Double.parseDouble(Float.valueOf(intent.getFloatExtra("volume_sold", 0f)).toString());
                transactionStarted = true;
                if (transType == TransactionValueType.Amount.ordinal()) {
                        if (amount >= transValue){
                        transComplete = true;
                    }
                    if (transValue > 0 && amount > 0) {
                        percentage = (int) ((amount / transValue) * 100);
                    }
                }
                else if (transType == TransactionValueType.Volume.ordinal()) {
                    if (volume >= transValue){
                        transComplete = true;
                    }
                    if (transValue > 0 && volume > 0) {
                        percentage = (int) ((volume / transValue) * 100);
                    }
                }

                if (transactionState == TransactionState.ST_PUMP_FILL_COMP){
                    transComplete = true;
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (transType == TransactionValueType.Amount.ordinal()) {
                        txtValueType.setText("Amount Authorized");
                    }
                    else if (transType == TransactionValueType.Volume.ordinal()) {
                        txtValueType.setText("Volume Authorized");
                    }
                    if (transactionState == TransactionState.ST_PUMP_AUTH || transactionState == TransactionState.ST_PUMP_FILLING || transactionState == TransactionState.ST_PUMP_FILL_COMP) {
                        layoutTrans.setVisibility(View.VISIBLE);
                        txtAmount.setText(Utility.convert2DecimalString(amount, false));
                        txtVolume.setText(String.format("%s L", Utility.convert2DecimalString(volume, false)));
                        txtAuthorizedAmount.setText(Utility.convert2DecimalString(transValue, false));
                    }
                    else {
                        layoutTrans.setVisibility(View.GONE);
                    }
                    String transState = TransactionState.getString(transactionState, pumpDisplayName);
                    String pState = PumpState.getString(pumpState);

                    if (transactionState == TransactionState.ST_ERROR || transactionState == TransactionState.ST_LIB_ERROR ||
                            transactionState == TransactionState.ST_PUMP_BUSY /*|| transactionState == TransactionState.ST_IDLE*/) {
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

                    if (transactionAck == 1){
                        if (mCallback != null){
                            mCallback.onStarted();
                        }
                    }

                    if (transactionState == TransactionState.ST_PUMP_AUTH &&
                            (pumpState == PumpState.NOZZLE_HANG_UP || pumpState == PumpState.PUMP_AUTH_NOZZLE_HANG_UP)){
                        transState = PumpState.getString(pumpState);
                        if (mCallback != null){
                            mCallback.onStarted();
                        }
                    }
                    txtTransState.setText(transState);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        progressBar.setProgress(percentage, true);
                    }
                    else{
                        progressBar.setProgress(percentage);
                    }

                    txtProgress.setText(String.format("%d%%", percentage));

                    if(transComplete){
                        if (constraintLayout != null){
                            constraintLayout.setBackgroundColor(getResources().getColor(R.color.libraryTranCompleteColor));
                        }

                        if (btnEndTrans != null){
                            btnEndTrans.setBackgroundColor(getResources().getColor(R.color.libraryColorPrimary));
                            btnEndTrans.setTextColor(getResources().getColor(R.color.libraryColorWhite));
                        }
                    }
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

        constraintLayout = findViewById(R.id.constraintLayout);
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
        img_conn_mode = findViewById(R.id.img_conn_mode);
        if (getIntent() != null){
            transactionDate = getIntent().getLongExtra("Transaction_Date", 0);
            pumpName = getIntent().getStringExtra("Pump_Name");
            pumpDisplayName = getIntent().getStringExtra("Pump_Display_Name");
            vouchercardNumber = getIntent().getStringExtra("voucher_card_number");
            connectionMode = getIntent().getStringExtra("connection_mode");

            if (connectionMode != null && !connectionMode.isEmpty()){
                if(connectionMode.equalsIgnoreCase("bluetooth")){
                    img_conn_mode.setImageResource(R.drawable.ic_bluetooth);
                }
                else if(connectionMode.equalsIgnoreCase("wifi")){
                    img_conn_mode.setImageResource(R.drawable.ic_wifi);
                }
            }
        }

        btnEndTrans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnData = new Intent();
                returnData.putExtra("sessionId", sessionId);
                returnData.putExtra("volume", volume);
                returnData.putExtra("amount", amount);
                returnData.putExtra("transactionValue", transValue);
                returnData.putExtra("transactionStarted", transactionStarted);
                returnData.putExtra("transactionDate", transactionDate);
                returnData.putExtra("voucherCardNumber", vouchercardNumber);
                returnData.putExtra("transactionCompleted", transComplete);
                return_value = -1;
                setResult(return_value, returnData);
                finish();

                mCallback.onCompleted(return_value, returnData);
            }
        });

        imgDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                returnData = new Intent();
                returnData.putExtra("sessionId", sessionId);
                returnData.putExtra("volume", volume);
                returnData.putExtra("amount", amount);
                returnData.putExtra("transactionValue", transValue);
                returnData.putExtra("transactionStarted", transactionStarted);
                returnData.putExtra("transactionDate", transactionDate);
                returnData.putExtra("transactionCompleted", transComplete);
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

        mCallback.onCompleted(return_value, returnData);
    }

    public static void setCallback(TransactionCallback callback){
        mCallback = callback;
    }
}