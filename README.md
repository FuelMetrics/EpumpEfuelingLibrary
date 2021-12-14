# EpumpEfuelingLibrary

Add this to the module's build.gradle file
implementation 'com.github.FuelMetrics.EpumpEfuelingLibrary:EfuelingLibrary:0.0.1-beta03'

To start interaction with the library call the line below;
EfuelingConnect efuelingConnect = EfuelingConnect.getInstance(context);
efuelingConnect.init(Common.SERVER_KEY, terminalId);

//For connection to the device
//To be done on a new thread

//wifi
efuelingConnect.turnWifi(true);
efuelingConnect.connect2WifiAndSocket(ssid, password, ip_address);

//bluetooth
if (efuelingConnect.initBluetooth(mac_address)){
   efuelingConnect.startBLE();
}

//to start transaction
efuelingConnect.startTransaction(transactionType, pumpName, pumpDisplayName, tag, amount, new TransactionCallback() {
	@Override
        public void onStarted() {
                
        }

        @Override
        public void onCompleted() {

        }
});

