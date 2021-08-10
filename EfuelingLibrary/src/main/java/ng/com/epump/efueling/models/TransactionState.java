package ng.com.epump.efueling.models;

public class TransactionState {
    public static final int ST_INIT = 0;
    public static final int ST_IDLE = 1;
    public static final int ST_PUMP_BUSY = 2;
    public static final int ST_REQUESTING_FROM_SERVER = 3;
    public static final int ST_PUMP_AUTH = 4;
    public static final int ST_PUMP_FILLING = 5;
    public static final int ST_PUMP_FILL_COMP = 6;
    public static final int ST_NULL = 7;
    public static final int ST_ERROR = 8;

    public static String getString(int state) {
        switch (state) {
            case ST_INIT:
                return "Go setting up";
            case ST_IDLE:
                return "Please start a transaction";
            case ST_PUMP_BUSY:
                return "Pump not ready";
            case ST_REQUESTING_FROM_SERVER:
                return "Processing request";
            case ST_PUMP_AUTH:
                return "Transaction authorized, Pick Up Nozzle ";
            case ST_PUMP_FILLING:
                return "Transaction in progress";
            case ST_PUMP_FILL_COMP:
                return "Transaction completed";
            case ST_NULL:
                return "Ready state";
            case ST_ERROR:
                return "Transaction error: ";
            default:
                break;
        }
        return "";
    }
}
