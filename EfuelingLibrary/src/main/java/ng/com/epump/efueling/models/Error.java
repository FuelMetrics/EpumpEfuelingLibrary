package ng.com.epump.efueling.models;

import android.util.Log;

import java.util.Calendar;

public class Error {
    public static String getError(String error){
        String errorString = "";
        if (error.startsWith("ERROR[")){
            error = error.substring(6, error.length() - 1);
        }
        error = error.trim();
        if (error.toUpperCase().contains("NULL")){
            return errorString;
        }
        String[] err = error.split(":");
        String errorType = err[0];
        Log.i("TAG", "getError: " + error);
        int errorCode = err.length > 1 ? Integer.parseInt(err[1]) : -1;
        int errorMessageCode = err.length > 2 ? Integer.parseInt(err[2]) : -1;
        if (errorType.equalsIgnoreCase(ErrorType.G.name())){
            if (errorCode == GoErrorType.EVT_SERVER_ERROR.ordinal()){
                errorString = ServerErrorType.getString(errorMessageCode);
            }
            else if (errorCode == GoErrorType.EVT_SOCKET_ERROR.ordinal()){
                errorString = "Network Error";
            }
            else if (errorCode == GoErrorType.EVT_VALUE_IS_ZERO.ordinal()){
                errorString = "Value cannot be zero";
            }
            else if (errorCode == GoErrorType.EVT_VALUE_IS_TOO_LOW.ordinal()){
                errorString = "Transaction value too low";
            }
            else {
                errorString = "Unknown Error";
            }
        }
        else if (errorType.equalsIgnoreCase(ErrorType.L.name())){
            errorString = "Library - " + LibraryErrorType.getString(errorCode);
        }
        String time = Utility.parseDate(Calendar.getInstance().getTime(), "EEE MMM dd, yyyy hh:mm aa");
        return errorString + " - " + error +  "\n" + time;
    }
}
