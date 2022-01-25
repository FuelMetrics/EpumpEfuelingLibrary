package ng.com.epump.pos.isw.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.gson.Gson;
import com.interswitchng.smartpos.IswTxnHandler;
import com.interswitchng.smartpos.models.CardModel;
import com.interswitchng.smartpos.models.PaymentModel;
import com.interswitchng.smartpos.models.core.TerminalInfo;
import com.interswitchng.smartpos.models.transaction.CardReadTransactionResponse;
import com.interswitchng.smartpos.models.transaction.PaymentType;
import com.interswitchng.smartpos.models.transaction.TransactionResult;
import com.interswitchng.smartpos.models.transaction.cardpaycode.CardType;
import com.interswitchng.smartpos.models.transaction.cardpaycode.EmvMessage;
import com.interswitchng.smartpos.models.transaction.cardpaycode.EmvResult;
import com.interswitchng.smartpos.models.transaction.cardpaycode.request.AccountType;
import com.interswitchng.smartpos.models.transaction.cardpaycode.request.PurchaseType;
import com.interswitchng.smartpos.shared.services.iso8583.utils.IsoUtils;

import java.util.Objects;
import java.util.concurrent.CancellationException;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.GlobalScope;
import kotlinx.coroutines.channels.Channel;
import kotlinx.coroutines.channels.ChannelIterator;
import kotlinx.coroutines.channels.SendChannel;
import kotlinx.coroutines.channels.ValueOrClosed;
import kotlinx.coroutines.selects.SelectClause1;
import kotlinx.coroutines.selects.SelectClause2;
import ng.com.epump.pos.isw.EMVMessageProcessor;
import ng.com.epump.pos.isw.ISW_POS;
import ng.com.epump.pos.isw.MessageResult;
import ng.com.epump.pos.isw.R;

public class CardTransactionActivity extends AppCompatActivity {
    Context context;
    LinearLayout pinLayout;
    ConstraintLayout layoutInsertCard, layoutEnterPin, layoutProcessingTransaction;
    TextView txtAmount, txtPin1, txtPin2, txtPin3, txtPin4;
    ProgressBar progressReadCard;
    Button btnContinue;
    BottomSheetDialog bottomSheetDialog;
    IswTxnHandler iswTxnHandler;
    TerminalInfo terminalInfo;
    CoroutineScope coroutineScope;
    EMVMessageProcessor _emvProcessor;
    AccountType accountType;
    Gson gson;
    int amount;
    int resultCode = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_transaction);

        context = this;
        gson = new Gson();

        iswTxnHandler = ISW_POS.iswTxnHandler;
        coroutineScope = GlobalScope.INSTANCE;
        _emvProcessor = new EMVMessageProcessor();

        amount = getIntent().getIntExtra("amount_to_pay", 0);
        String amountString = getIntent().getStringExtra("amount_to_pay_string");

        layoutInsertCard = findViewById(R.id.layoutInsertCard);
        layoutEnterPin = findViewById(R.id.layoutEnterPin);
        layoutProcessingTransaction = findViewById(R.id.layoutProcessingTransaction);
        btnContinue = findViewById(R.id.btnContinue);
        progressReadCard = findViewById(R.id.progressReadCard);
        txtAmount = findViewById(R.id.txtAmount);
        txtPin1 = findViewById(R.id.txtPin1);
        txtPin2 = findViewById(R.id.txtPin2);
        txtPin3 = findViewById(R.id.txtPin3);
        txtPin4 = findViewById(R.id.txtPin4);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressReadCard.setVisibility(View.VISIBLE);
                btnContinue.setEnabled(false);
                initiateTransaction(amount);
            }
        });

        txtAmount.setText(String.format("NGN %s", amountString));
    }

    private void initiateTransaction(int amountToPay){
        terminalInfo = iswTxnHandler.getTerminalInfo();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                iswTxnHandler.setupTransaction(amountToPay, Objects.requireNonNull(terminalInfo), coroutineScope, new Channel<EmvMessage>() {
                    @Override
                    public boolean isClosedForReceive() {
                        return false;
                    }

                    @Override
                    public boolean isEmpty() {
                        return false;
                    }

                    @Nullable
                    @Override
                    public Object receive(@NonNull Continuation<? super EmvMessage> continuation) {
                        return null;
                    }

                    @NonNull
                    @Override
                    public SelectClause1<EmvMessage> getOnReceive() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public Object receiveOrNull(@NonNull Continuation<? super EmvMessage> continuation) {
                        return null;
                    }

                    @NonNull
                    @Override
                    public SelectClause1<EmvMessage> getOnReceiveOrNull() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public Object receiveOrClosed(@NonNull Continuation<? super ValueOrClosed<? extends EmvMessage>> continuation) {
                        return null;
                    }

                    @NonNull
                    @Override
                    public SelectClause1<ValueOrClosed<EmvMessage>> getOnReceiveOrClosed() {
                        return null;
                    }

                    @Nullable
                    @Override
                    public EmvMessage poll() {
                        return null;
                    }

                    @NonNull
                    @Override
                    public ChannelIterator<EmvMessage> iterator() {
                        return null;
                    }

                    @Override
                    public void cancel(@Nullable CancellationException e) {

                    }

                    @Override
                    public boolean isClosedForSend() {
                        return false;
                    }

                    @Override
                    public boolean isFull() {
                        return false;
                    }

                    @Nullable
                    @Override
                    public Object send(EmvMessage emvMessage, @NonNull Continuation<? super Unit> continuation) {
                        processEMVResponse(emvMessage);
                        return emvMessage;
                    }

                    @NonNull
                    @Override
                    public SelectClause2<EmvMessage, SendChannel<EmvMessage>> getOnSend() {
                        return null;
                    }

                    @Override
                    public boolean offer(EmvMessage emvMessage) {
                        return false;
                    }

                    @Override
                    public boolean close(@Nullable Throwable throwable) {
                        return false;
                    }

                    @Override
                    public void invokeOnClose(@NonNull Function1<? super Throwable, Unit> function1) {

                    }
                }, new Continuation<Unit>() {
                    @NonNull
                    @Override
                    public CoroutineContext getContext() {
                        return coroutineScope.getCoroutineContext();
                    }

                    @Override
                    public void resumeWith(@NonNull Object o) {
                    }
                });
            }
        }).start();
    }

    void processEMVResponse(EmvMessage message){
        MessageResult result = _emvProcessor.processMessage(message);
        Log.i("TAG", "processEMVResponse: " + result.getMessageType());
        /*if (result.getMessageType().equalsIgnoreCase("Insert_Card")) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "Please Insert Card", Toast.LENGTH_LONG).show();
                }
            });
        }
        else */
        if (result.getMessageType().equalsIgnoreCase("Card_Read") && result.getCardType() != CardType.None){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showAccountType(result.getCardType());
                }
            });
        }
        else if (result.getMessageType().equalsIgnoreCase("Enter_Pin")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    layoutInsertCard.setVisibility(View.GONE);
                    layoutEnterPin.setVisibility(View.VISIBLE);
                }
            });
        }
        else if (result.getMessageType().equalsIgnoreCase("Transaction_Cancelled")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    resultCode = -2;
                    Intent intent = new Intent();
                    intent.putExtra("message", result.getMessage());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setResult(resultCode, intent);
                            finish();
                        }
                    });
                }
            });
        }
        else if (result.getMessageType().equalsIgnoreCase("Pin_Text")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String p1, p2, p3, p4;
                    String pin = result.getPinText();
                    switch (pin.length()){
                        case 1 :
                            txtPin1.setText(pin);
                            txtPin2.setText("");
                            txtPin3.setText("");
                            txtPin4.setText("");
                            break;
                        case 2 :
                            p1 = String.valueOf(pin.charAt(0));
                            p2 = String.valueOf(pin.charAt(1));
                            txtPin1.setText(p1);
                            txtPin2.setText(p2);
                            txtPin3.setText("");
                            txtPin4.setText("");
                            break;
                        case 3 :
                            p1 = String.valueOf(pin.charAt(0));
                            p2 = String.valueOf(pin.charAt(1));
                            p3 = String.valueOf(pin.charAt(2));
                            txtPin1.setText(p1);
                            txtPin2.setText(p2);
                            txtPin3.setText(p3);
                            txtPin4.setText("");
                            break;
                        case 4 :
                            p1 = String.valueOf(pin.charAt(0));
                            p2 = String.valueOf(pin.charAt(1));
                            p3 = String.valueOf(pin.charAt(2));
                            p4 = String.valueOf(pin.charAt(3));
                            txtPin1.setText(p1);
                            txtPin2.setText(p2);
                            txtPin3.setText(p3);
                            txtPin4.setText(p4);
                            break;
                        default:
                            txtPin1.setText("");
                            txtPin2.setText("");
                            txtPin3.setText("");
                            txtPin4.setText("");
                            break;
                    }
                }
            });
        }
        else if (result.getMessageType().equalsIgnoreCase("Processing_Transaction") ||
                result.getMessageType().equalsIgnoreCase("Pin_Ok")){
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    layoutEnterPin.setVisibility(View.GONE);
                    layoutProcessingTransaction.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void startTransaction(CardType cardType, AccountType accountType) {
        Object result = iswTxnHandler.startTransaction(new Continuation<EmvResult>() {
            @NonNull
            @Override
            public CoroutineContext getContext() {
                return coroutineScope.getCoroutineContext();
            }

            @Override
            public void resumeWith(@NonNull Object o) {

            }
        });

        EmvResult emvResult = (EmvResult) result;
        if (emvResult == EmvResult.OFFLINE_APPROVED || emvResult == EmvResult.ONLINE_REQUIRED) {
            processTransaction(cardType, accountType);
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, emvResult.name(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    void showAccountType(CardType cardType){
        View dialogView = ((Activity)context).getLayoutInflater().inflate(R.layout.account_type_bottom_sheet, null);
        RelativeLayout rlDefault = dialogView.findViewById(R.id.rlDefault);
        RelativeLayout rlSavings = dialogView.findViewById(R.id.rlSavings);
        RelativeLayout rlCurrent = dialogView.findViewById(R.id.rlCurrent);
        RelativeLayout rlCredit = dialogView.findViewById(R.id.rlCredit);
        TextView txtCancel = dialogView.findViewById(R.id.txtCancel);

        rlDefault.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetDialog != null) {
                    bottomSheetDialog.cancel();
                }
                accountType = AccountType.Default;
                continueTransaction(cardType, accountType);
            }
        });

        rlSavings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetDialog != null) {
                    bottomSheetDialog.cancel();
                }
                accountType = AccountType.Savings;
                continueTransaction(cardType, accountType);
            }
        });

        rlCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetDialog != null) {
                    bottomSheetDialog.cancel();
                }
                accountType = AccountType.Current;
                continueTransaction(cardType, accountType);
            }
        });

        rlCredit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetDialog != null) {
                    bottomSheetDialog.cancel();
                }
                accountType = AccountType.Credit;
                continueTransaction(cardType, accountType);
            }
        });

        txtCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bottomSheetDialog != null) {
                    bottomSheetDialog.cancel();
                }

                Intent intent = new Intent();
                intent.putExtra("message", "User Cancelled");
                resultCode = -2;
                setResult(resultCode, intent);
                finish();
            }
        });

        bottomSheetDialog = new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();
    }

    void continueTransaction(CardType cardType, AccountType accountType){
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
                startTransaction(cardType, accountType);
            }
        }).start();
    }

    private void processTransaction(CardType cardType, AccountType accountType){
        PaymentModel pm = new PaymentModel(amount, String.valueOf(amount),
                PaymentModel.TransactionType.CARD_PURCHASE, PaymentType.Card, null, null,
                null, null, null,null, cardType);
        CardReadTransactionResponse response = iswTxnHandler.processCardTransaction(pm,
                accountType, terminalInfo, PurchaseType.Card);
        TransactionResult result = response.getTransactionResult();
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
        intent.putExtra("pos_transaction_result", gson.toJson(response));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setResult(resultCode, intent);
                finish();
            }
        });
    }
}