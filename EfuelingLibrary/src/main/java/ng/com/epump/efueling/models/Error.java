package ng.com.epump.efueling.models;

public class Error {
    public static String getError(String error){
        String errorString = "";
        if (error.startsWith("ERROR[")){
            error = error.substring(6, error.length() - 1);
        }
        error = error.trim();
        if (error.equalsIgnoreCase("NULL")){
            return errorString;
        }
        String[] err = error.split(":");
        String errorType = err[0];
        int errorCode = Integer.parseInt(err[1]);
        int errorMessageCode = Integer.parseInt(err[2]);
        if (errorType.equalsIgnoreCase(ErrorType.G.name())){
            if (errorCode == GoErrorType.EVT_SERVER_ERROR.ordinal()){
                errorString = ServerErrorType.getString(errorMessageCode);
            }
            else if (errorCode == GoErrorType.EVT_SOCKET_ERROR.ordinal()){
                errorString = "Network Error";
            }
            else {
                errorString = "Unknown Error";
            }
        }
        else if (errorType.equalsIgnoreCase(ErrorType.L.name())){
            errorString = "Library - " + LibraryErrorType.getString(errorCode);
        }
        return errorString + " - " + error;
    }
}
