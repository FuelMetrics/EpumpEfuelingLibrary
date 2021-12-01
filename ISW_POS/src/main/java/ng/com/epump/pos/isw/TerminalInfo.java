package ng.com.epump.pos.isw;

public class TerminalInfo {
    private String agentEmail;
    private String agentId;
    private int callHomeTimeInMin;
    private String capabilities;
    private String countryCode;
    private String currencyCode;
    private boolean isKimono;
    private boolean isKimono3;
    private String merchantCategoryCode;
    private String merchantId;
    private String merchantNameAndLocation;
    private String serverIp;
    private int serverPort;
    private int serverTimeoutInSec;
    private String serverUrl;
    private String terminalId;

    public TerminalInfo(com.interswitchng.smartpos.models.core.TerminalInfo info) {
        new TerminalInfo(info.getAgentEmail(), info.getAgentId(), info.getCallHomeTimeInMin(),
                info.getCapabilities(), info.getCountryCode(), info.getCurrencyCode(), info.isKimono(),
                info.isKimono3(), info.getMerchantCategoryCode(), info.getMerchantId(), info.getMerchantNameAndLocation(),
                info.getServerIp(), info.getServerPort(), info.getServerTimeoutInSec(), info.getServerUrl(), info.getTerminalId());
    }

    public TerminalInfo(String agentEmail, String agentId, int callHomeTimeInMin, String capabilities,
                        String countryCode, String currencyCode, boolean isKimono, boolean isKimono3,
                        String merchantCategoryCode, String merchantId, String merchantNameAndLocation,
                        String serverIp, int serverPort, int serverTimeoutInSec, String serverUrl,
                        String terminalId) {
        this.agentEmail = agentEmail;
        this.agentId = agentId;
        this.callHomeTimeInMin = callHomeTimeInMin;
        this.capabilities = capabilities;
        this.countryCode = countryCode;
        this.currencyCode = currencyCode;
        this.isKimono = isKimono;
        this.isKimono3 = isKimono3;
        this.merchantCategoryCode = merchantCategoryCode;
        this.merchantId = merchantId;
        this.merchantNameAndLocation = merchantNameAndLocation;
        this.serverIp = serverIp;
        this.serverPort = serverPort;
        this.serverTimeoutInSec = serverTimeoutInSec;
        this.serverUrl = serverUrl;
        this.terminalId = terminalId;
    }

    public String getAgentEmail() {
        return agentEmail;
    }

    public void setAgentEmail(String agentEmail) {
        this.agentEmail = agentEmail;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public int getCallHomeTimeInMin() {
        return callHomeTimeInMin;
    }

    public void setCallHomeTimeInMin(int callHomeTimeInMin) {
        this.callHomeTimeInMin = callHomeTimeInMin;
    }

    public String getCapabilities() {
        return capabilities;
    }

    public void setCapabilities(String capabilities) {
        this.capabilities = capabilities;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public boolean isKimono() {
        return isKimono;
    }

    public void setKimono(boolean kimono) {
        isKimono = kimono;
    }

    public boolean isKimono3() {
        return isKimono3;
    }

    public void setKimono3(boolean kimono3) {
        isKimono3 = kimono3;
    }

    public String getMerchantCategoryCode() {
        return merchantCategoryCode;
    }

    public void setMerchantCategoryCode(String merchantCategoryCode) {
        this.merchantCategoryCode = merchantCategoryCode;
    }

    public String getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(String merchantId) {
        this.merchantId = merchantId;
    }

    public String getMerchantNameAndLocation() {
        return merchantNameAndLocation;
    }

    public void setMerchantNameAndLocation(String merchantNameAndLocation) {
        this.merchantNameAndLocation = merchantNameAndLocation;
    }

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getServerTimeoutInSec() {
        return serverTimeoutInSec;
    }

    public void setServerTimeoutInSec(int serverTimeoutInSec) {
        this.serverTimeoutInSec = serverTimeoutInSec;
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }
}
