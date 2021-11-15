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
    public static final int ST_LIB_ERROR = 9;

    public static String getString(int state, String pumpDisplayName) {
        String resp = "";
        //String pName = (pumpName != null && pumpName.length > 0) ? removeAlphabets(pumpName[0]) : "";
        switch (state) {
            case ST_INIT:
                resp = "Go setting up";
                break;
            case ST_IDLE:
                resp = String.format("Initializing %s...", pumpDisplayName);
                break;
            case ST_PUMP_BUSY:
                resp = String.format("Pump (%s) not ready", pumpDisplayName);
                break;
            case ST_REQUESTING_FROM_SERVER:
                resp = String.format("Processing request on %s...", pumpDisplayName);
                break;
            case ST_PUMP_AUTH:
                resp = String.format("Transaction authorized, Pick Up %s",  pumpDisplayName);
                break;
            case ST_PUMP_FILLING:
                resp = String.format("Transaction in progress on %s...", pumpDisplayName);
                break;
            case ST_PUMP_FILL_COMP:
                resp = String.format("Transaction completed on %s", pumpDisplayName);
                break;
            case ST_NULL:
                resp = String.format("Ready state on %s", pumpDisplayName);
                break;
            case ST_ERROR:
                resp = String.format("Transaction error on %s:", pumpDisplayName);
                break;
            case ST_LIB_ERROR:
                resp = String.format("Library error on %s:", pumpDisplayName);
                break;
            default:
                resp = "-" + state;
        }
        return resp;
    }

    private static String removeAlphabets(String str){
        return str.replaceAll("[^\\d.]", "");
    }
}
