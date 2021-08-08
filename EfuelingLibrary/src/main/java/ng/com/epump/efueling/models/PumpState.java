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
        switch (state) {
            case PUMP_NOT_ACTIVE:
                return "Pump is not active";
            case PUMP_NOT_LOCKED:
                return "Pump is not locked";
            case NOZZLE_HANG_DOWN:
                return "Nozzle down";
            case NOZZLE_HANG_UP:
                return "Nozzle up";
            case PUMP_AUTH_NOZZLE_HANG_DOWN:
                return "Pump authorized, nozzle down";
            case PUMP_AUTH_NOZZLE_HANG_UP:
                return "Pump authorized, nozzle up";
            case PUMP_FILLING:
                return "Pump currently selling";
            case PUMP_FILLED_LIMIT:
                return "Pump finished selling";
            case PUMP_FILL_COMP_NOZZLE_HANG_DOWN:
                return "Pump finished selling, nozzle down";
            case PUMP_FILL_COMP_NOZZLE_HANG_UP:
                return "Pump finished selling, nozzle up";
            case PUMP_SWITCHED_OFF:
                return "Pump is switched off";
            case PUMP_STATUS_OTHERS:
                return "Pump status unknown";
            case PUMP_ERROR:
                return "Pump error";
            case PUMP_STATUS_UNKNOWN:
                return "Pump offline";
            default:
                break;
        }
        return "";
    }
}
