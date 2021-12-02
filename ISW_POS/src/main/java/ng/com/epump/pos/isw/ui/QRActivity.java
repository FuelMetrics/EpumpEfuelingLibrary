package ng.com.epump.pos.isw.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.interswitchng.smartpos.IswTxnHandler;
import com.interswitchng.smartpos.models.cardless.CardLessPaymentRequest;
import com.interswitchng.smartpos.models.core.TerminalInfo;
import com.interswitchng.smartpos.models.transaction.PaymentType;
import com.interswitchng.smartpos.models.transaction.ussdqr.response.Bank;
import com.interswitchng.smartpos.models.transaction.ussdqr.response.CodeResponse;
import com.interswitchng.smartpos.models.transaction.ussdqr.response.PaymentStatus;
import com.interswitchng.smartpos.shared.models.transaction.CardLessPaymentInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.GlobalScope;
import ng.com.epump.pos.isw.ISW_POS;
import ng.com.epump.pos.isw.Payment_Status;
import ng.com.epump.pos.isw.QR_USSD_PaymentStatus;
import ng.com.epump.pos.isw.QR_USSD_TransactionResult;
import ng.com.epump.pos.isw.R;

public class QRActivity extends AppCompatActivity {
    Context context;
    ConstraintLayout layoutQR, layoutProcessingTransaction;
    Button btnContinue, btnCancel;
    TextView txtAmount;
    Spinner spinnerBank;
    ImageView qrImage;
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
        setContentView(R.layout.activity_qr);

        context = this;
        gson = new Gson();

        iswTxnHandler = ISW_POS.iswTxnHandler;
        coroutineScope = GlobalScope.INSTANCE;

        amount = getIntent().getIntExtra("amount_to_pay", 0);
        String amountString = getIntent().getStringExtra("amount_to_pay_string");

        terminalInfo = iswTxnHandler.getTerminalInfo();

        layoutQR = findViewById(R.id.layoutQR);
        layoutProcessingTransaction = findViewById(R.id.layoutProcessingTransaction);
        btnContinue = findViewById(R.id.btnContinue);
        btnCancel = findViewById(R.id.btnCancel);
        txtAmount = findViewById(R.id.txtAmount);
        qrImage = findViewById(R.id.qrImage);

        txtAmount.setText(String.format("NGN %s", amountString));


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
                                terminalInfo.getMerchantCode(), PaymentType.QR, new Continuation<PaymentStatus>() {
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

        initiateQRTransaction();
    }

    void initiateQRTransaction(){
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
                        CardLessPaymentRequest.TRANSACTION_QR, CardLessPaymentRequest.QR_FORMAT_RAW, null);
                iswTxnHandler.initiateQrTransactions(request, context, new Continuation<CodeResponse>() {
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
                                    Toast.makeText(context, "An error occurred", Toast.LENGTH_LONG).show();
                                }
                                else{
                                    String shortCode = response.getBankShortCode() == null ? response.getDefaultShortCode() : response.getBankShortCode();
                                    if (response.getResponseCode().equals("00")) {
                                        qrImage.setVisibility(View.VISIBLE);
                                        qrImage.setImageBitmap(response.getQrCodeImage());
                                        //setExistImage(qrImage, response.getQrCodeData());
                                        btnContinue.setEnabled(true);
                                    } else {
                                        Toast.makeText(context, String.format("%s - %s", response.getResponseCode(), response.getResponseDescription()), Toast.LENGTH_LONG).show();
                                    }
                                }
                            }
                        });
                    }
                });
            }
        }).start();
    }

    Bitmap generateQR(String data){
        QRCodeWriter writer = new QRCodeWriter();
        try {
            Map<EncodeHintType, ErrorCorrectionLevel> mp = new HashMap<>();
            mp.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
            BitMatrix bitMatrix = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 200, 200, mp);
            //BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, 200, 200);
            int width = bitMatrix.getWidth();
            int height = bitMatrix.getHeight();
            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int color = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? getColor(R.color.libraryColorPrimary) : Color.BLACK;
                    bmp.setPixel(x, y, bitMatrix.get(x, y) ? color : Color.WHITE);
                }
            }
            return bmp;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setExistImage(ImageView imageView, String base64String){
        if (!base64String.isEmpty()) {
            byte[] bytes = Base64.decode(base64String, Base64.DEFAULT);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        }
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