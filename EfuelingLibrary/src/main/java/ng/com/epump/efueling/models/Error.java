package ng.com.epump.efueling.models;

public class Error {
    public static String getError(String error){
        String errorString = "";
        String[] err = error.split(";");
        String errorType = err[0];
        String errorCode = err[1];
        String errorMessageCode = err[2];
        if (errorType.equalsIgnoreCase(ErrorType.G.name())){
            if (errorCode.equalsIgnoreCase(GoErrorType.EVT_SERVER_ERROR.name())){
                errorString = ServerErrorType.getString(Integer.parseInt(errorMessageCode));
            }
            else if (errorCode.equalsIgnoreCase(GoErrorType.EVT_SOCKET_ERROR.name())){
                errorString = "Network Error";
            }
        }
        else if (errorType.equalsIgnoreCase(ErrorType.G.name())){
            errorString = "Library Error - " + errorCode;
        }
        return errorString;
    }
}
