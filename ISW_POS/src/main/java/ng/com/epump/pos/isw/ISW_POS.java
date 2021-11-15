package ng.com.epump.pos.isw;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.WorkerParameters;

import com.interswitch.smartpos.emv.telpo.TelpoPOSDeviceImpl;
import com.interswitchng.smartpos.IswPos;
import com.interswitchng.smartpos.IswTxnHandler;
import com.interswitchng.smartpos.emv.pax.services.POSDeviceImpl;
import com.interswitchng.smartpos.models.core.Environment;
import com.interswitchng.smartpos.models.core.POSConfig;
import com.interswitchng.smartpos.models.core.TerminalInfo;
import com.interswitchng.smartpos.models.posconfig.PrintObject;
import com.interswitchng.smartpos.models.posconfig.PrintStringConfiguration;
import com.interswitchng.smartpos.models.printer.info.PrintStatus;
import com.interswitchng.smartpos.models.transaction.cardpaycode.EmvMessage;
import com.interswitchng.smartpos.shared.interfaces.device.POSDevice;
import com.interswitchng.smartpos.shared.services.kimono.models.AllTerminalInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CancellationException;

import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.jvm.functions.Function1;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.CoroutineScopeKt;
import kotlinx.coroutines.channels.Channel;
import kotlinx.coroutines.channels.ChannelIterator;
import kotlinx.coroutines.channels.SendChannel;
import kotlinx.coroutines.channels.ValueOrClosed;
import kotlinx.coroutines.selects.SelectClause1;
import kotlinx.coroutines.selects.SelectClause2;

public class ISW_POS {
    private PrintStringConfiguration bold, boldCenter, boldTitleCenter, normalTitleCenter, normalCenter, normal;

    private static POSDevice posDevice;
    private static POSConfig posConfig;
    private static IswTxnHandler iswTxnHandler;
    private static CoroutineScope coroutineScope;
    private static Context mContext;
    static String clientId = "IKIAB23A4E2756605C1ABC33CE3C287E27267F660D61";
    static String clientSecret = "secret";
    static String alias = "000007";
    static String merchantCode = "MX5882";
    static String phoneNumber= "20390007";

    public ISW_POS(Context context, boolean pax) {
        if (mContext == null){
            Init(context, pax);
        }
        coroutineScope = CoroutineScopeKt.CoroutineScope((CoroutineContext) mContext);
    }

    private static void Init(Context context, boolean pax) {
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
        BackgroundWorker.iswTxnHandler = iswTxnHandler;
        WorkRequest request = new OneTimeWorkRequest.Builder(BackgroundWorker.class).build();
        WorkManager.getInstance(context).enqueue(request);
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

        PrintStatus status = iswTxnHandler.checkPrintStatus();
        String msg = status.getMessage();



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

    public void Pay(double amount, PaymentCallback callback) throws Exception{
        if (posDevice == null){
            throw new Exception("Call init method to initialize library");
        }
        int amountToPay = (int) (amount * 100);
        TerminalInfo terminalInfo = iswTxnHandler.getTerminalInfo();
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
                return null;
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
                Log.i("TAG", "resumeWith: ");
            }
        });
        /*IswPos.getInstance().pay(amountToPay, new IswPos.IswPaymentCallback() {
            @Override
            public void onUserCancel() {
                callback.onCancel();
            }

            @Override
            public void onPaymentCompleted(@NonNull IswTransactionResult iswTransactionResult) {
                ISW_TransactionResult result = new ISW_TransactionResult(iswTransactionResult.getResponseCode(),
                        iswTransactionResult.getResponseMessage(), iswTransactionResult.isSuccessful(),
                        iswTransactionResult.getTransactionReference(),
                        iswTransactionResult.getAmount(), iswTransactionResult.getCardType().name(),
                        iswTransactionResult.getTransactionType().name());
                callback.onPaymentComplete(result);
            }
        }, new Transaction.Purchase((PaymentType) null), 0, 0);*/
    }

    /*public static void goToSettings(){
        IswPos.showSettingsScreen();
    }*/
}
