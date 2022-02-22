package ng.com.epump.pos.isw.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.interswitchng.smartpos.IswTxnHandler;
import com.interswitchng.smartpos.models.core.TerminalInfo;
import com.interswitchng.smartpos.models.transaction.PaymentInfo;
import com.interswitchng.smartpos.models.transaction.TransactionResult;
import com.interswitchng.smartpos.shared.models.transaction.CardLessPaymentInfo;
import com.interswitchng.smartpos.shared.services.iso8583.utils.IsoUtils;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.GlobalScope;
import ng.com.epump.pos.isw.ISW_POS;
import ng.com.epump.pos.isw.R;

public class PayCodeActivity extends AppCompatActivity {
    Context context;
    ConstraintLayout layoutInputCode, layoutProcessingTransaction;
    Button btnContinue;
    TextInputEditText txtPayCode;
    TextView txtAmount;
    IswTxnHandler iswTxnHandler;
    TerminalInfo terminalInfo;
    CoroutineScope coroutineScope;
    Gson gson;
    int amount;
    int resultCode = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pay_code);

        context = this;
        gson = new Gson();

        iswTxnHandler = ISW_POS.iswTxnHandler;
        coroutineScope = GlobalScope.INSTANCE;

        amount = getIntent().getIntExtra("amount_to_pay", 0);
        String amountString = getIntent().getStringExtra("amount_to_pay_string");

        layoutInputCode = findViewById(R.id.layoutInputCode);
        layoutProcessingTransaction = findViewById(R.id.layoutProcessingTransaction);
        btnContinue = findViewById(R.id.btnContinue);
        txtAmount = findViewById(R.id.txtAmount);
        txtPayCode = findViewById(R.id.txtPayCode);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnContinue.setEnabled(false);
                initiateTransaction(amount);
            }
        });

        txtAmount.setText(String.format("NGN %s", amountString));
    }

    private void initiateTransaction(int amountToPay) {
        terminalInfo = iswTxnHandler.getTerminalInfo();
        layoutInputCode.setVisibility(View.GONE);
        layoutProcessingTransaction.setVisibility(View.VISIBLE);
        String payCode = txtPayCode.getText().toString();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                if (terminalInfo.getMerchantCode() == null || terminalInfo.getMerchantCode().equals("")){
                    terminalInfo.setMerchantCode(ISW_POS.merchantCode);
                    terminalInfo.setMerchantAlias(ISW_POS.alias);
                }
                iswTxnHandler.getToken(terminalInfo, new Continuation<Unit>() {
                    @NonNull
                    @Override
                    public CoroutineContext getContext() {
                        return coroutineScope.getCoroutineContext();
                    }

                    @Override
                    public void resumeWith(@NonNull Object o) {

                    }
                });

                TransactionResult result = iswTxnHandler.processPayCode(terminalInfo,
                        new CardLessPaymentInfo(amountToPay, "", 0, 0), payCode);

                if (result != null){
                    resultCode = -1;
                    switch (result.getResponseCode()){
                        case IsoUtils.TIMEOUT_CODE:
                            Log.d("TAG", "Transaction status: timeout");
                            break;
                        case IsoUtils.OK:
                            Log.d("TAG", "Transaction status: success");
                            break;
                        default:
                            break;
                    }
                }

                Intent intent = new Intent();
                intent.putExtra("pos_transaction_result", gson.toJson(result));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setResult(resultCode, intent);
                        finish();
                    }
                });
            }
        }).start();
    }
}