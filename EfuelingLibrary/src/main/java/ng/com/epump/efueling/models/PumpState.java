package ng.com.epump.efueling.models;

import androidx.annotation.NonNull;

public class PumpState {
    public static final int PUMP_NOT_ACTIVE = 0;
    public static final int PUMP_NOT_LOCKED = 1;
    public static final int NOZZLE_HANG_DOWN = 2;
    public static final int NOZZLE_HANG_UP = 3;
    public static final int PUMP_AUTH_NOZZLE_HANG_DOWN = 4;
    public static final int PUMP_AUTH_NOZZLE_HANG_UP = 5;
    public static final int PUMP_FILLING = 6;
    public static final int PUMP_FILLED_LIMIT = 7;
    public static final int PUMP_FILL_COMP_NOZZLE_HANG_DOWN = 8;
    public static final int PUMP_FILL_COMP_NOZZLE_HANG_UP = 9;
    public static final int PUMP_SWITCHED_OFF = 252;
    public static final int PUMP_STATUS_OTHERS = 253;
    public static final int PUMP_ERROR = 254;
    public static final int PUMP_STATUS_UNKNOWN = 255;

    public static String getString(int state) {
        String resp = "";
        switch (state) {
            case PUMP_NOT_ACTIVE:
                resp = "Pump is not active";
                break;
            case PUMP_NOT_LOCKED:
                resp = "Pump is not locked";
                break;
            case NOZZLE_HANG_DOWN:
                resp = "Nozzle down";
                break;
            case NOZZLE_HANG_UP:
                resp = "Nozzle up";
                break;
            case PUMP_AUTH_NOZZLE_HANG_DOWN:
                resp = "Pump authorized, nozzle down";
                break;
            case PUMP_AUTH_NOZZLE_HANG_UP:
                resp = "Pump authorized, nozzle up";
                break;
            case PUMP_FILLING:
                resp = "Pump currently selling";
                break;
            case PUMP_FILLED_LIMIT:
                resp = "Pump finished selling";
                break;
            case PUMP_FILL_COMP_NOZZLE_HANG_DOWN:
                resp = "Pump finished selling, nozzle down";
                break;
            case PUMP_FILL_COMP_NOZZLE_HANG_UP:
                resp = "Pump finished selling, nozzle up";
                break;
            case PUMP_SWITCHED_OFF:
                resp = "Pump is switched off";
                break;
            case PUMP_STATUS_OTHERS:
                resp = "Pump status unknown";
                break;
            case PUMP_ERROR:
                resp = "Pump error";
                break;
            case PUMP_STATUS_UNKNOWN:
                resp = "Pump offline";
                break;
            default:
                resp = "-" + state;
                break;
        }

        return resp;
    }
}
