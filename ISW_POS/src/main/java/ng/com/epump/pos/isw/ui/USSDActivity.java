package ng.com.epump.pos.isw.ui;

import static android.graphics.Typeface.BOLD;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.interswitchng.smartpos.IswTxnHandler;
import com.interswitchng.smartpos.models.cardless.CardLessPaymentRequest;
import com.interswitchng.smartpos.models.core.TerminalInfo;
import com.interswitchng.smartpos.models.transaction.PaymentInfo;
import com.interswitchng.smartpos.models.transaction.PaymentType;
import com.interswitchng.smartpos.models.transaction.ussdqr.request.TransactionInfo;
import com.interswitchng.smartpos.models.transaction.ussdqr.response.Bank;
import com.interswitchng.smartpos.models.transaction.ussdqr.response.CodeResponse;
import com.interswitchng.smartpos.models.transaction.ussdqr.response.PaymentStatus;
import com.interswitchng.smartpos.models.transaction.ussdqr.response.Transaction;
import com.interswitchng.smartpos.shared.models.transaction.CardLessPaymentInfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.GlobalScope;
import ng.com.epump.pos.isw.ISW_POS;
import ng.com.epump.pos.isw.IswTransactionType;
import ng.com.epump.pos.isw.Payment_Status;
import ng.com.epump.pos.isw.QR_USSD_PaymentStatus;
import ng.com.epump.pos.isw.QR_USSD_TransactionResult;
import ng.com.epump.pos.isw.R;

public class USSDActivity extends AppCompatActivity {
    Context context;
    ConstraintLayout layoutUSSD, layoutProcessingTransaction;
    Button btnContinue, btnCancel;
    TextView txtAmount, txtShortCode;
    Spinner spinnerBank;
    IswTxnHandler iswTxnHandler;
    TerminalInfo terminalInfo;
    CoroutineScope coroutineScope;
    Gson gson;
    ArrayList<String> bankString;
    List<Bank> banks;
    CodeResponse response;
    int amount;
    int resultCode = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ussd);

        context = this;
        gson = new Gson();

        iswTxnHandler = ISW_POS.iswTxnHandler;
        coroutineScope = GlobalScope.INSTANCE;

        amount = getIntent().getIntExtra("amount_to_pay", 0);
        String amountString = getIntent().getStringExtra("amount_to_pay_string");

        terminalInfo = iswTxnHandler.getTerminalInfo();

        layoutUSSD = findViewById(R.id.layoutUSSD);
        layoutProcessingTransaction = findViewById(R.id.layoutProcessingTransaction);
        btnContinue = findViewById(R.id.btnContinue);
        btnCancel = findViewById(R.id.btnCancel);
        txtAmount = findViewById(R.id.txtAmount);
        txtShortCode = findViewById(R.id.txtShortCode);
        spinnerBank = findViewById(R.id.spinnerBank);

        txtAmount.setText(String.format("NGN %s", amountString));

        spinnerBank.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0){
                    Bank bank = banks.get(position - 1);
                    btnContinue.setEnabled(false);
                    layoutProcessingTransaction.setVisibility(View.VISIBLE);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            CardLessPaymentInfo paymentInfo = new CardLessPaymentInfo(
                                    amount, "", 0,0
                            );
                            if (terminalInfo.getMerchantCode() == null || terminalInfo.getMerchantCode().equals("")){
                                terminalInfo.setMerchantCode(ISW_POS.merchantCode);
                                terminalInfo.setMerchantAlias(ISW_POS.alias);
                            }
                            CardLessPaymentRequest request = CardLessPaymentRequest.Companion.from(terminalInfo, paymentInfo,
                                    CardLessPaymentRequest.TRANSACTION_USSD, null, bank.getCode());
                            iswTxnHandler.initiateUssdTransactions(request, new Continuation<CodeResponse>() {
                                @NonNull
                                @Override
                                public CoroutineContext getContext() {
                                    return coroutineScope.getCoroutineContext();
                                }

                                @Override
                                public void resumeWith(@NonNull Object o) {
                                    response = (CodeResponse) o;
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            layoutProcessingTransaction.setVisibility(View.GONE);
                                            if(response == null){
                                                Toast.makeText(context, "An error occured", Toast.LENGTH_LONG).show();
                                            }
                                            else{
                                                String shortCode = response.getBankShortCode() == null ? response.getDefaultShortCode() : response.getBankShortCode();
                                                if (response.getResponseCode().equals("00")) {
                                                    String text2 = "Dial " + shortCode + " to complete transaction, then press the Continue button";
                                                    Spannable spannable = new SpannableString(text2);
                                                    spannable.setSpan(new RelativeSizeSpan(1.5f), 5, shortCode.length() + 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                    spannable.setSpan(new StyleSpan(BOLD), 5, shortCode.length() + 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                                                    txtShortCode.setText(spannable, TextView.BufferType.SPANNABLE);
                                                    btnContinue.setEnabled(true);
                                                } else {
                                                    txtShortCode.setText(String.format("%s - %s", response.getResponseCode(), response.getResponseDescription()));
                                                }
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }).start();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("message", "User Cancelled");
                resultCode = -2;
                setResult(resultCode, intent);
                finish();
            }
        });

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnContinue.setEnabled(false);
                layoutProcessingTransaction.setVisibility(View.VISIBLE);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Object object = iswTxnHandler.checkPaymentStatus(response.getTransactionReference(),
                                terminalInfo.getMerchantCode(), PaymentType.USSD, new Continuation<PaymentStatus>() {
                                    @NonNull
                                    @Override
                                    public CoroutineContext getContext() {
                                        return coroutineScope.getCoroutineContext();
                                    }

                                    @Override
                                    public void resumeWith(@NonNull Object responseObject) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                layoutProcessingTransaction.setVisibility(View.GONE);
                                                PaymentStatus status = (PaymentStatus) responseObject;
                                                QR_USSD_TransactionResult result = new QR_USSD_PaymentStatus().getResponse(status);
                                                if (result.getPaymentStatus() == Payment_Status.PENDING) {
                                                    btnContinue.setEnabled(true);
                                                    Toast.makeText(context, result.getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                                else {
                                                    resultCode = -1;
                                                    Intent intent = new Intent();
                                                    intent.putExtra("pos_transaction_result", gson.toJson(result));
                                                    setResult(resultCode, intent);
                                                    finish();
                                                }
                                            }
                                        });
                                    }
                                });
                    }
                }).start();
            }
        });

        loadBanks();
    }

    void loadBanks(){
        layoutProcessingTransaction.setVisibility(View.VISIBLE);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                iswTxnHandler.loadbanks(new Continuation<List<Bank>>() {
                    @NonNull
                    @Override
                    public CoroutineContext getContext() {
                        return coroutineScope.getCoroutineContext();
                    }

                    @Override
                    public void resumeWith(@NonNull Object bankObject) {
                        banks =(List<Bank>) bankObject;
                        bankString = new ArrayList<>();
                        bankString.add("Select Bank");
                        for (Bank bank: banks){
                            bankString.add(bank.getName());
                        }

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                layoutProcessingTransaction.setVisibility(View.GONE);
                                ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>
                                        (context, android.R.layout.simple_spinner_item,
                                                bankString); //selected item will look like a spinner set from XML
                                spinnerArrayAdapter.setDropDownViewResource(android.R.layout
                                        .simple_spinner_dropdown_item);
                                spinnerBank.setAdapter(spinnerArrayAdapter);
                            }
                        });
                    }
                });
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent();
        intent.putExtra("message", "User Cancelled");
        resultCode = -2;
        setResult(resultCode, intent);
        finish();
    }
}