package ng.com.epump.pos.isw;

import ng.com.epump.pos.isw.bridge.TerminalInfo;

public interface TerminalInfoCallback {
    void onSuccess(TerminalInfo terminalInfo, String serialNumber);
}
