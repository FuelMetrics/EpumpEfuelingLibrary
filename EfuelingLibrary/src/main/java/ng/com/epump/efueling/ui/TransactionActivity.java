package ng.com.epump.efueling.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import ng.com.epump.efueling.R;
import ng.com.epump.efueling.models.PumpState;
import ng.com.epump.efueling.models.TransactionState;

public class TransactionActivity extends AppCompatActivity {
    private Context context;
    private TextView txtPumpState, txtTransState;
    private Button button;
    private int pumpState, transactionState;
    private String errorString = "";
    BroadcastReceiver infoReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pumpState = intent.getIntExtra("pump_state", 0);
            transactionState = intent.getIntExtra("transaction_state", 0);
            errorString = intent.getStringExtra("transaction_error");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String transState = TransactionState.getString(transactionState);
                    String pState = PumpState.getString(pumpState);
                    if (pumpState == 8) {
                        transState = transState + errorString;
                    }
                    txtTransState.setText(transState);

                    txtPumpState.setText(pState);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction);
        context = this;

        txtPumpState = findViewById(R.id.txtPumpState);
        txtTransState = findViewById(R.id.txtTransState);
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