package ng.com.epump.efueling.models;

public class ServerErrorType {
    public static final int Successful = 0;
    public static final int NoValidEndPoint = 101;
    public static final int InternalServerError = 102;
    public static final int InvalidJsonString = 103;
    public static final int DeviceNotFound = 104;
    public static final int TankNotFound = 105;
    public static final int PumpNotFound = 106;
    public static final int ExistingSales = 107;
    public static final int NoDeviceSales = 1016;
    public static final int NOScanStartRecord = 108;
    public static final int EventNotRecorgnized = 109;
    public static final int ExpiredCard = 110;
    public static final int CardNotTrusted = 111;
    public static final int CardLocked = 112;
    public static final int CardNotFound = 113;
    public static final int CardNotActivated = 125;
    public static final int CardNotAssigned = 126;
    public static final int CardTransactionExist = 127;
    public static final int VoucherUsed = -114;
    public static final int VoucherNotFound = -115;
    public static final int VoucherCannotBeProcessed = -116;
    public static final int VoucherNotAllowed = -118;
    public static final int VoucherCanceledByOwner = -117;
    public static final int InsufficientBalance = 118;
    public static final int UserWalletNotFound = 119;
    public static final int IncorrectPassword = 120;
    public static final int QuickFuelNotFound = 121;
    public static final int UserNotFound = 122;
    public static final int POSReferenceUsed = 123;
    public static final int POSReferenceNotFound = 124;

    public static String getString(int errorCode){
        switch (errorCode) {
            case Successful:
                return "Successful";
            case NoValidEndPoint:
                return "No Valid EndPoint";
            case InternalServerError:
                return "Internal Server Error";
            case InvalidJsonString:
                return "Invalid Json String";
            case DeviceNotFound:
                return "Device Not Found";
            case TankNotFound:
                return "Tank Not Found";
            case PumpNotFound:
                return "Pump Not Found";
            case ExistingSales:
                return "Existing Sales";
            case NoDeviceSales:
                return "No Device Sales";
            case NOScanStartRecord:
                return "NO Scan Start Record";
            case EventNotRecorgnized:
                return "Event Not Recorgnized";
            case ExpiredCard:
                return "Expired Card";
            case CardNotTrusted:
                return "Card Not Trusted";
            case CardLocked:
                return "Card Locked";
            case CardNotFound:
                return "Card Not Found";
            case CardNotActivated:
                return "Card Not Activated";
            case CardNotAssigned:
                return "Card Not Assigned";
            case CardTransactionExist:
                return "Card Transaction Exist";
            case VoucherUsed:
                return "Voucher Previously Used";
            case VoucherNotFound:
                return "Voucher Not Found";
            case VoucherCannotBeProcessed:
                return "Voucher Cannot Be Processed";
            case VoucherNotAllowed:
                return "Voucher Not Allowed";
            case VoucherCanceledByOwner:
                return "Voucher Canceled By Owner";
            case InsufficientBalance:
                return "Insufficient Balance";
            case UserWalletNotFound:
                return "User Wallet Not Found";
            case IncorrectPassword:
                return "Incorrect Password";
            case QuickFuelNotFound:
                return "QuickFuel Not Found";
            case UserNotFound:
                return "User Not Found";
            case POSReferenceUsed:
                return "POS Reference Previously Used";
            case POSReferenceNotFound:
                return "POS Reference Not Found";
            default:
                return "Unknown server error";
        }
    }
}
