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

    public static String getString(int state, String... pumpName) {
        String resp = "";
        String pName = (pumpName != null && pumpName.length > 0) ? (pumpName[0].length() > 1 ? pumpName[0].substring(1) : pumpName[0]) : "";
        switch (state) {
            case ST_INIT:
                resp = "Go setting up";
                break;
            case ST_IDLE:
                resp = "Initializing...";
                break;
            case ST_PUMP_BUSY:
                resp = "Pump not ready";
                break;
            case ST_REQUESTING_FROM_SERVER:
                resp = "Processing request...";
                break;
            case ST_PUMP_AUTH:
                resp = "Transaction authorized, Pick Up Nozzle " + pName;
                break;
            case ST_PUMP_FILLING:
                resp = "Transaction in progress";
                break;
            case ST_PUMP_FILL_COMP:
                resp = "Transaction completed";
                break;
            case ST_NULL:
                resp = "Ready state";
                break;
            case ST_ERROR:
                resp = "Transaction error: ";
                break;
            default:
                resp = "-" + state;
        }
        return resp;
    }
}
