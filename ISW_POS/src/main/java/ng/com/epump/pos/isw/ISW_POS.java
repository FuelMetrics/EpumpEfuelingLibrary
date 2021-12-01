package ng.com.epump.pos.isw;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Process;
import android.util.Log;

import androidx.annotation.NonNull;

import com.interswitch.smartpos.emv.telpo.TelpoPOSDeviceImpl;
import com.interswitchng.smartpos.IswPos;
import com.interswitchng.smartpos.IswTxnHandler;
import com.interswitchng.smartpos.emv.pax.services.POSDeviceImpl;
import com.interswitchng.smartpos.models.core.Environment;
import com.interswitchng.smartpos.models.core.POSConfig;
import com.interswitchng.smartpos.models.posconfig.PrintObject;
import com.interswitchng.smartpos.models.posconfig.PrintStringConfiguration;
import com.interswitchng.smartpos.models.printer.info.TransactionType;
import com.interswitchng.smartpos.models.transaction.TransactionLog;
import com.interswitchng.smartpos.shared.interfaces.device.POSDevice;
import com.interswitchng.smartpos.shared.services.kimono.models.AllTerminalInfo;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.GlobalScope;
import ng.com.epump.pos.isw.ui.CardTransactionActivity;

public class ISW_POS {
    private PrintStringConfiguration bold, boldCenter, boldTitleCenter, normalTitleCenter, normalCenter, normal;

    private static POSDevice posDevice;
    private static POSConfig posConfig;
    public static IswTxnHandler iswTxnHandler;
    private static CoroutineScope coroutineScope;
    private Context mContext;
    static String clientId = "IKIAB23A4E2756605C1ABC33CE3C287E27267F660D61";
    static String clientSecret = "secret";
    static String alias = "000007";
    static String merchantCode = "MX5882";
    static String phoneNumber= "20390007";
    private static TerminalInfo terminalInfo;
    private static com.interswitchng.smartpos.models.core.TerminalInfo iswInfo;

    public ISW_POS(Context context, boolean pax, TerminalInfoCallback callback) {
        if (mContext == null){
            Init(context, pax, callback);
        }
        coroutineScope = GlobalScope.INSTANCE;
    }

    private void Init(Context context, boolean pax, TerminalInfoCallback callback) {
        mContext = context;
        if (pax){
            posDevice = POSDeviceImpl.create(context.getApplicationContext());
        }
        else{
            posDevice = TelpoPOSDeviceImpl.create(context);
        }
        posConfig = new POSConfig(alias, clientId, clientSecret, merchantCode, phoneNumber, Environment.Test);
        IswPos.setupTerminal((Application) context.getApplicationContext(), posDevice,
                null, posConfig, false, false);

        iswTxnHandler = new IswTxnHandler(posDevice);
        iswInfo = iswTxnHandler.getTerminalInfo();
        if (iswInfo != null){
            terminalInfo = new TerminalInfo(iswInfo);
        }

        if (terminalInfo == null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                    AllTerminalInfo allInfo = (AllTerminalInfo) iswTxnHandler.downloadTmKimParam(new Continuation<AllTerminalInfo>() {
                        @NonNull
                        @Override
                        public CoroutineContext getContext() {
                            return coroutineScope.getCoroutineContext();
                        }

                        @Override
                        public void resumeWith(@NonNull Object o) {
                            Log.i("TAG", "resumeWith: ");
                        }
                    });

                    if (allInfo != null && allInfo.getResponseCode().equalsIgnoreCase("00")){
                        iswInfo = iswTxnHandler.getTerminalInfoFromResponse(allInfo);
                        terminalInfo = new TerminalInfo(iswInfo);
                        iswTxnHandler.saveTerminalInfo(iswInfo);
                        callback.onSuccess(terminalInfo, iswTxnHandler.getSerialNumber());
                    }
                }
            }).start();
        }
        else{
            callback.onSuccess(terminalInfo, iswTxnHandler.getSerialNumber());
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                boolean keyDownloaded = iswTxnHandler.downloadKeys(terminalInfo.getTerminalId(), terminalInfo.getServerIp(), terminalInfo.getServerPort(), true);
                if (keyDownloaded){
                    iswTxnHandler.getToken(iswInfo, new Continuation<Unit>() {
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
            }
        }).start();
    }

    public void PrintTransaction(PrintTransactionModel model) throws Exception{
        if (posDevice == null){
            throw new Exception("Call init method to initialize library");
        }
        bold = new PrintStringConfiguration(false, true, false);
        boldCenter = new PrintStringConfiguration(false, true, true);
        boldTitleCenter = new PrintStringConfiguration(true, true, true);
        normalCenter = new PrintStringConfiguration(false, false, true);
        normalTitleCenter = new PrintStringConfiguration(true, false, true);
        normal = new PrintStringConfiguration(false, false, false);

        PrintObject line = PrintObject.Line.INSTANCE;

        List<PrintObject> printObjects = new ArrayList<>();
        printObjects.add(new PrintObject.Data(model.getCopyTitle(), boldTitleCenter));
        printObjects.add(new PrintObject.Data(model.getStationName(), bold));
        printObjects.add(new PrintObject.Data(model.getStationAddress(), bold));
        printObjects.add(new PrintObject.Data("Trans. Id: " + model.getTransactionId(), normal));
        printObjects.add(new PrintObject.Data("\n", normal));
        printObjects.add(new PrintObject.Data("Date: " + model.getDate(), normal));
        printObjects.add(new PrintObject.Data("\n", normal));
        printObjects.add(new PrintObject.Data("Time: " + model.getTime(), normal));
        printObjects.add(new PrintObject.Data("\n", normal));
        printObjects.add(line);
        printObjects.add(new PrintObject.Data("\n", normal));
        printObjects.add(new PrintObject.Data("Channel: " + model.getTransactionChannel(), normal));
        printObjects.add(new PrintObject.Data("Product: " + model.getProduct(), normal));
        printObjects.add(new PrintObject.Data("\n", normal));
        printObjects.add(line);
        printObjects.add(new PrintObject.Data("\n", normal));
        printObjects.add(new PrintObject.Data("Amount: " + model.getAmount(), bold));
        printObjects.add(new PrintObject.Data("\n", normal));
        printObjects.add(new PrintObject.Data("Volume: " + model.getVolume(), bold));
        printObjects.add(new PrintObject.Data("\n", normal));
        printObjects.add(line);
        printObjects.add(new PrintObject.Data("\n", normal));
        printObjects.add(new PrintObject.Data("Thanks for using Epump.", normalCenter));
        printObjects.add(new PrintObject.Data("\n", normal));
        printObjects.add(new PrintObject.Data("www.epump.com.ng", normalCenter));
        printObjects.add(new PrintObject.Data("\n", normal));
        printObjects.add(new PrintObject.Data("support@epump.com.ng", normalCenter));
        printObjects.add(new PrintObject.Data("\n", normal));
        printObjects.add(new PrintObject.Data("01-6327474-5", normalTitleCenter));
        printObjects.add(new PrintObject.Data("\n", normal));

        String msg = iswTxnHandler.checkPrintStatus().getMessage();



        /*IswPos.getInstance().print(printObjects, new IswPos.IswPrinterCallback() {
            @Override
            public void onError(@NonNull IswPrintResult iswPrintResult) {
                Log.i("TAG", "onError: " + iswPrintResult.getMessage());
            }

            @Override
            public void onPrintCompleted(@NonNull IswPrintResult iswPrintResult) {
                if (model.isCustomerCopy()){
                    model.setCustomerCopy(false);
                    try {
                        PrintTransaction(model);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });*/
    }

    public void printEOD(List<TransactionLog> transactionLogs, String date){
        iswTxnHandler.printEod(TransactionType.Purchase,transactionLogs,date, iswInfo);
    }

    public PrintStatus printTransaction(Bitmap bitmap){
        com.interswitchng.smartpos.models.printer.info.PrintStatus status = iswTxnHandler.printslip(bitmap);
        return new PrintStatus(status.getMessage());
    }

    public void setupTransaction(double amount, String amountString) throws Exception{
        if (posDevice == null){
            throw new Exception("Call init method to initialize library");
        }
        int amountToPay = (int) (amount * 100);
        iswInfo = iswTxnHandler.getTerminalInfo();
        terminalInfo = new TerminalInfo(iswInfo);

        Intent intent = new Intent(mContext, CardTransactionActivity.class);
        intent.putExtra("amount_to_pay", amountToPay);
        intent.putExtra("amount_to_pay_string", amountString);
        ((Activity)(mContext)).startActivityForResult(intent, 234);
    }

    public PrintStatus printerStatus(){
        com.interswitchng.smartpos.models.printer.info.PrintStatus status = iswTxnHandler.checkPrintStatus();
        return new PrintStatus(status.getMessage());
    }

    public void getTerminal(TerminalInfoCallback callback){
        iswInfo = iswTxnHandler.getTerminalInfo();
        final TerminalInfo[] terminalInfo = {new TerminalInfo(iswInfo)};
        if (terminalInfo[0] == null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

                    AllTerminalInfo allInfo = (AllTerminalInfo) iswTxnHandler.downloadTmKimParam(new Continuation<AllTerminalInfo>() {
                        @NonNull
                        @Override
                        public CoroutineContext getContext() {
                            return coroutineScope.getCoroutineContext();
                        }

                        @Override
                        public void resumeWith(@NonNull Object o) {
                            Log.i("TAG", "resumeWith: ");
                        }
                    });

                    if (allInfo != null && allInfo.getResponseCode().equalsIgnoreCase("00")){
                        iswInfo = iswTxnHandler.getTerminalInfoFromResponse(allInfo);
                        terminalInfo[0] = new TerminalInfo(iswInfo);
                        iswTxnHandler.saveTerminalInfo(iswInfo);
                    }

                    if (terminalInfo[0] != null){
                        callback.onSuccess(terminalInfo[0], iswTxnHandler.getSerialNumber());
                    }
                }
            }).start();
        }
    }

    public String getSerialNumber(){
        return iswTxnHandler.getSerialNumber();
    }

    /*public static void goToSettings(){
        IswPos.showSettingsScreen();
    }*/
}
