package ng.com.epump.efueling.models;

public class LibraryErrorType {
    public static final int EP_FSM_EVT_UNDEF = 0;
    public static final int EP_FSM_EVT_INIT = 1;
    public static final int EP_FSM_EVT_START_TR = 2;
    public static final int EP_FSM_EVT_STOP_TR = 3;
    public static final int EP_FSM_EVT_TIMEOUT = 4;
    public static final int EP_FSM_EVT_NO_RESP_TIMEOUT = 5;
    public static final int EP_FSM_EVT_UNACK_ERROR_WRONG_STATE = 6;
    public static final int EP_FSM_EVT_UNACK_ERROR_WRONG_SESSION = 7;
    public static final int EP_FSM_EVT_GO_ERROR = 8;    //on this event, the app gets the correct error message from go
    public static final int EP_FSM_EVT_UNDEF_GO_ERROR = 9;
    public static final int EP_FSM_EVT_CLEAR_ERROR = 10;

    public static String getString(int code){
        switch (code) {
            case EP_FSM_EVT_UNDEF:
                return "Undefined Error";
            case EP_FSM_EVT_INIT:
                return "Initialization";
            case EP_FSM_EVT_START_TR:
                return "Start Transaction";
            case EP_FSM_EVT_STOP_TR:
                return "StarStopt Transaction";
            case EP_FSM_EVT_TIMEOUT:
                return "Timeout";
            case EP_FSM_EVT_NO_RESP_TIMEOUT:
                return "No Response Timeout";
            case EP_FSM_EVT_UNACK_ERROR_WRONG_STATE:
                return "Wrong State Error";
            case EP_FSM_EVT_UNACK_ERROR_WRONG_SESSION:
                return "Wrong Session Error";
            case EP_FSM_EVT_GO_ERROR:
                return "GO Error";
            case EP_FSM_EVT_UNDEF_GO_ERROR:
                return "Undefined GO Error";
            case EP_FSM_EVT_CLEAR_ERROR:
                return "Clear Error";
            default:
                return "";
        }
    }
}
