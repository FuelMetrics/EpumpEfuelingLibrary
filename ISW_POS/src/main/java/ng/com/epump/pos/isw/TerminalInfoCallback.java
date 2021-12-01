package ng.com.epump.pos.isw;

public interface TerminalInfoCallback {
    void onSuccess(TerminalInfo terminalInfo, String serialNumber);
}
