package ng.com.epump.pos.isw;

import com.interswitchng.smartpos.models.core.TerminalInfo;

public interface TerminalInfoCallback {
    void onSuccess(TerminalInfo terminalInfo, String serialNumber);
}
