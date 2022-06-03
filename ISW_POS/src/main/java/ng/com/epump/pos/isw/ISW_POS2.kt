package ng.com.epump.pos.isw

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Process
import android.util.Log
import com.interswitch.smartpos.emv.telpo.TelpoPOSDeviceImpl
import com.interswitchng.smartpos.IswPos
import com.interswitchng.smartpos.IswTxnHandler
import com.interswitchng.smartpos.emv.pax.services.POSDeviceImpl
import com.interswitchng.smartpos.models.core.Environment
import com.interswitchng.smartpos.models.core.POSConfig
import com.interswitchng.smartpos.models.posconfig.PrintObject
import com.interswitchng.smartpos.models.posconfig.PrintStringConfiguration
import com.interswitchng.smartpos.models.printer.info.TransactionType
import com.interswitchng.smartpos.models.transaction.TransactionLog
import com.interswitchng.smartpos.shared.interfaces.device.POSDevice
import com.interswitchng.smartpos.shared.services.kimono.models.AllTerminalInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ng.com.epump.pos.isw.bridge.PrintStatus
import ng.com.epump.pos.isw.bridge.TerminalInfo
import ng.com.epump.pos.isw.ui.CardTransactionActivity
import ng.com.epump.pos.isw.ui.PayCodeActivity
import ng.com.epump.pos.isw.ui.QRActivity
import ng.com.epump.pos.isw.ui.USSDActivity
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext

public class ISW_POS2(context: Context, pax: Boolean, callback: TerminalInfoCallback) {
    private var bold: PrintStringConfiguration? = null
    private var boldCenter: PrintStringConfiguration? = null
    private var boldTitleCenter: PrintStringConfiguration? = null
    private var normalTitleCenter: PrintStringConfiguration? = null
    private var normalCenter: PrintStringConfiguration? = null
    private var normal: PrintStringConfiguration? = null
    private var mContext: Context? = null
    private fun Init(context: Context, pax: Boolean, callback: TerminalInfoCallback) {
        mContext = context
        if (pax) {
            posDevice = POSDeviceImpl.create(context.applicationContext)
        } else {
            posDevice = TelpoPOSDeviceImpl.create(context)
        }
        posConfig = POSConfig(
            alias,
            clientId,
            clientSecret,
            merchantCode,
            phoneNumber,
            Environment.Production
        )
        IswPos.setupTerminal(
            (context.applicationContext as Application),
            posDevice!!,
            null, posConfig!!, false, false
        )
        iswTxnHandler = IswTxnHandler(posDevice)
        iswInfo = iswTxnHandler!!.getTerminalInfo()
        if (iswInfo != null) {
            terminalInfo = TerminalInfo.getTerminalInfo(iswInfo)
        }
        if (terminalInfo == null || terminalInfo!!.terminalId == null || terminalInfo!!.terminalId.isEmpty()) {
            runBlocking {
                launch {
                    val allInfo = iswTxnHandler!!.downloadTmKimParam()

                    if (allInfo != null && allInfo.responseCode.equals("00", ignoreCase = true)) {
                        iswInfo =
                            iswTxnHandler!!.getTerminalInfoFromResponse(
                                allInfo
                            )
                        terminalInfo =
                            TerminalInfo.getTerminalInfo(iswInfo)
                        iswInfo!!.isKimono = false
                        iswTxnHandler!!.saveTerminalInfo(iswInfo!!)
                        callback.onSuccess(
                            terminalInfo!!,
                            iswTxnHandler!!.getSerialNumber()
                        )

                        val keyDownloaded =
                            iswTxnHandler!!.downloadNibssKeys(
                                terminalInfo!!.getTerminalId(),
                                terminalInfo!!.getServerIp(),
                                terminalInfo!!.getServerPort(),
                                false
                            )
                        if (keyDownloaded) {
                            iswTxnHandler!!.getToken(
                                iswInfo!!)
                        }
                    }
                }
            }
        } else {
            callback.onSuccess(terminalInfo!!, iswTxnHandler!!.getSerialNumber())
            runBlocking {
                launch {
                    val keyDownloaded =
                        iswTxnHandler!!.downloadNibssKeys(
                            terminalInfo!!.terminalId,
                            terminalInfo!!.serverIp,
                            terminalInfo!!.serverPort,
                            true
                        )
                    if (keyDownloaded) {
                        iswTxnHandler!!.getToken(iswInfo!!)
                    }
                }
            }
        }
    }

    @Throws(Exception::class)
    fun PrintTransaction(model: PrintTransactionModel) {
        if (posDevice == null) {
            throw Exception("Call init method to initialize library")
        }
        bold = PrintStringConfiguration(false, true, false)
        boldCenter = PrintStringConfiguration(false, true, true)
        boldTitleCenter = PrintStringConfiguration(true, true, true)
        normalCenter = PrintStringConfiguration(false, false, true)
        normalTitleCenter = PrintStringConfiguration(true, false, true)
        normal = PrintStringConfiguration(false, false, false)
        val line: PrintObject = PrintObject.Line
        val printObjects: MutableList<PrintObject> = ArrayList()
        printObjects.add(PrintObject.Data(model.copyTitle, boldTitleCenter!!))
        printObjects.add(PrintObject.Data(model.stationName, bold!!))
        printObjects.add(PrintObject.Data(model.stationAddress, bold!!))
        printObjects.add(PrintObject.Data("Trans. Id: " + model.transactionId, normal!!))
        printObjects.add(PrintObject.Data("\n", normal!!))
        printObjects.add(PrintObject.Data("Date: " + model.date, normal!!))
        printObjects.add(PrintObject.Data("\n", normal!!))
        printObjects.add(PrintObject.Data("Time: " + model.time, normal!!))
        printObjects.add(PrintObject.Data("\n", normal!!))
        printObjects.add(line)
        printObjects.add(PrintObject.Data("\n", normal!!))
        printObjects.add(PrintObject.Data("Channel: " + model.transactionChannel, normal!!))
        printObjects.add(PrintObject.Data("Product: " + model.product, normal!!))
        printObjects.add(PrintObject.Data("\n", normal!!))
        printObjects.add(line)
        printObjects.add(PrintObject.Data("\n", normal!!))
        printObjects.add(PrintObject.Data("Amount: " + model.amount, bold!!))
        printObjects.add(PrintObject.Data("\n", normal!!))
        printObjects.add(PrintObject.Data("Volume: " + model.volume, bold!!))
        printObjects.add(PrintObject.Data("\n", normal!!))
        printObjects.add(line)
        printObjects.add(PrintObject.Data("\n", normal!!))
        printObjects.add(PrintObject.Data("Thanks for using Epump.", normalCenter!!))
        printObjects.add(PrintObject.Data("\n", normal!!))
        printObjects.add(PrintObject.Data("www.epump.com.ng", normalCenter!!))
        printObjects.add(PrintObject.Data("\n", normal!!))
        printObjects.add(PrintObject.Data("support@epump.com.ng", normalCenter!!))
        printObjects.add(PrintObject.Data("\n", normal!!))
        printObjects.add(PrintObject.Data("01-6327474-5", normalTitleCenter!!))
        printObjects.add(PrintObject.Data("\n", normal!!))
        val msg = iswTxnHandler!!.checkPrintStatus().message


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

    fun printEOD(transactionLogs: List<TransactionLog>?, date: String?) {
        iswTxnHandler!!.printEod(
            TransactionType.Purchase, transactionLogs!!,
            date!!,
            iswInfo!!
        )
    }

    fun printTransaction(bitmap: Bitmap?): PrintStatus {
        val status = iswTxnHandler!!.printslip(
            bitmap!!
        )
        return PrintStatus(status.message)
    }

    @Throws(Exception::class)
    fun setupTransaction(
        amount: Double,
        amountString: String?,
        transactionType: IswTransactionType?
    ) {
        if (posDevice == null) {
            throw Exception("Call init method to initialize library")
        }
        val amountToPay = (amount * 100).toInt()
        iswInfo = iswTxnHandler!!.getTerminalInfo()
        terminalInfo = TerminalInfo.getTerminalInfo(iswInfo)
        val intent: Intent
        when (transactionType) {
            IswTransactionType.CARD -> {
                intent = Intent(mContext, CardTransactionActivity::class.java)
                intent.putExtra("amount_to_pay", amountToPay)
                intent.putExtra("amount_to_pay_string", amountString)
                (mContext as Activity?)!!.startActivityForResult(intent, 234)
            }
            IswTransactionType.PAY_CODE -> {
                intent = Intent(mContext, PayCodeActivity::class.java)
                intent.putExtra("amount_to_pay", amountToPay)
                intent.putExtra("amount_to_pay_string", amountString)
                (mContext as Activity?)!!.startActivityForResult(intent, 234)
            }
            IswTransactionType.USSD -> {
                intent = Intent(mContext, USSDActivity::class.java)
                intent.putExtra("amount_to_pay", amountToPay)
                intent.putExtra("amount_to_pay_string", amountString)
                (mContext as Activity?)!!.startActivityForResult(intent, 234)
            }
            IswTransactionType.QR_CODE -> {
                intent = Intent(mContext, QRActivity::class.java)
                intent.putExtra("amount_to_pay", amountToPay)
                intent.putExtra("amount_to_pay_string", amountString)
                (mContext as Activity?)!!.startActivityForResult(intent, 234)
            }
            else -> {}
        }
    }

    fun printerStatus(): PrintStatus {
        val status = iswTxnHandler!!.checkPrintStatus()
        return PrintStatus(status.message)
    }

    fun getTerminal(callback: TerminalInfoCallback) {
        iswInfo = iswTxnHandler!!.getTerminalInfo()
        val terminalInfo = arrayOf(
            TerminalInfo.getTerminalInfo(
                iswInfo
            )
        )
        if (terminalInfo[0] == null) {
            runBlocking {
                launch {
                    val allInfo = iswTxnHandler!!.downloadTmKimParam()
                    if (allInfo != null && allInfo.responseCode.equals("00", ignoreCase = true)) {
                        iswInfo =
                            iswTxnHandler!!.getTerminalInfoFromResponse(
                                allInfo
                            )
                        terminalInfo[0] =
                            TerminalInfo.getTerminalInfo(iswInfo)
                        iswInfo!!.isKimono = false
                        iswTxnHandler!!.saveTerminalInfo(iswInfo!!)
                    }
                    if (terminalInfo[0] != null) {
                        callback.onSuccess(
                            terminalInfo[0],
                            iswTxnHandler!!.getSerialNumber()
                        )
                    }
                }
            }
        }
    }

    fun getSerialNumber(): String? {
        return iswTxnHandler!!.getSerialNumber()
    } /*public static void goToSettings(){
        IswPos.showSettingsScreen();
    }*/

    companion object {
        private var posDevice: POSDevice? = null
        private var posConfig: POSConfig? = null
        @JvmField
        var iswTxnHandler: IswTxnHandler? = null
        /*static String clientId = "IKIAB23A4E2756605C1ABC33CE3C287E27267F660D61";
    static String clientSecret = "secret";
    static String alias = "000007";
    static String merchantCode = "MX5882";
    static String phoneNumber= "20390007";*/
        @JvmField
        var clientId = "IKIA4733CE041F41ED78E52BD3B157F3AAE8E3FE153D"
        @JvmField
        var clientSecret = "t1ll73stS3cr3t"
        @JvmField
        var alias = "002208"
        @JvmField
        var merchantCode = "MX1065"
        @JvmField
        var phoneNumber = "080311402392"
        private var terminalInfo: TerminalInfo? = null
        private var iswInfo: com.interswitchng.smartpos.models.core.TerminalInfo? = null
    }

    init {
        if (mContext == null) {
            Init(context, pax, callback)
        }
    }
}