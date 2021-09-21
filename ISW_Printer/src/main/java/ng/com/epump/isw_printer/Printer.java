package ng.com.epump.isw_printer;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;

import com.interswitch.smartpos.emv.telpo.services.TelpoPOSDeviceImpl;
import com.interswitchng.smartpos.IswPos;
import com.interswitchng.smartpos.emv.pax.services.POSDeviceImpl;
import com.interswitchng.smartpos.shared.interfaces.device.POSDevice;
import com.interswitchng.smartpos.shared.models.core.Environment;
import com.interswitchng.smartpos.shared.models.core.POSConfig;
import com.interswitchng.smartpos.shared.models.posconfig.PrintObject;
import com.interswitchng.smartpos.shared.models.posconfig.PrintStringConfiguration;
import com.interswitchng.smartpos.shared.models.results.IswPrintResult;

import java.util.ArrayList;
import java.util.List;

public class Printer {
    private PrintStringConfiguration bold, boldCenter, normalCenter, normal;

    private POSDevice posDevice;
    private POSConfig posConfig;
    String clientId = "IKIAB23A4E2756605C1ABC33CE3C287E27267F660D61";
    String clientSecret = "secret";
    String alias = "000007";
    String merchantCode = "MX5882";
    String phoneNumber= "20390007";

    public Printer(Context context, boolean pax) {
        if (pax){
            posDevice = POSDeviceImpl.create(context.getApplicationContext());
        }
        else{
            posDevice = TelpoPOSDeviceImpl.create(context);
        }
        posConfig = new POSConfig(alias, clientId, clientSecret, merchantCode, phoneNumber, Environment.Test);
        IswPos.setupTerminal((Application) context.getApplicationContext(), posDevice, null, posConfig,
                true);
    }

    public void PrintTransaction(PrintTransactionModel model){
        bold = new PrintStringConfiguration(true, true, false);
        boldCenter = new PrintStringConfiguration(true, true, true);
        normalCenter = new PrintStringConfiguration(false, false, true);
        normal = new PrintStringConfiguration(false, false, false);

        PrintObject line = PrintObject.Line.INSTANCE;

        List<PrintObject> printObjects = new ArrayList<>();
        printObjects.add(new PrintObject.Data(model.getCopyTitle(), boldCenter));
        printObjects.add(new PrintObject.Data(model.getStationName(), boldCenter));
        printObjects.add(new PrintObject.Data(model.getStationAddress(), boldCenter));
        printObjects.add(new PrintObject.Data("\n", boldCenter));
        printObjects.add(new PrintObject.Data("Trans. Id: " + model.getTransactionId(), normal));
        printObjects.add(new PrintObject.Data("Date: " + model.getDate(), normal));
        printObjects.add(new PrintObject.Data("Time: " + model.getTime(), normal));
        printObjects.add(new PrintObject.Data("\n", boldCenter));
        printObjects.add(new PrintObject.Data("Channel: " + model.getTransactionChannel(), normal));
        printObjects.add(new PrintObject.Data("Product: " + model.getProduct(), normal));
        //printObjects.add(line);
        printObjects.add(new PrintObject.Data("\n", boldCenter));
        printObjects.add(new PrintObject.Data("Amount: " + model.getAmount(), bold));
        printObjects.add(new PrintObject.Data("Volume: " + model.getVolume(), bold));

        IswPos.getInstance().print(printObjects, new IswPos.IswPrinterCallback() {
            @Override
            public void onError(@NonNull IswPrintResult iswPrintResult) {

            }

            @Override
            public void onPrintCompleted(@NonNull IswPrintResult iswPrintResult) {
                if (model.isCustomerCopy()){
                    model.setCustomerCopy(false);
                    PrintTransaction(model);
                }
            }
        });
    }
}
