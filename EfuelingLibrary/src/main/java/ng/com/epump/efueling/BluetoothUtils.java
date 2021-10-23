package ng.com.epump.efueling;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ng.com.epump.efueling.interfaces.BluetoothUtilsCallback;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothUtils {
    public static final int REQUEST_ENABLE_BT = 516;
    private static final UUID SERVICE_UUID = UUID.fromString("0000a002-0000-1000-8000-00805f9b34fb");
    private static final UUID READ_XTC_UUID = UUID.fromString("0000c305-0000-1000-8000-00805f9b34fb");
    private static final UUID WRITE_XTC_UUID = UUID.fromString("0000c304-0000-1000-8000-00805f9b34fb");
    private final Context mContext;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private BluetoothGatt mGatt;
    private ScanSettings settings;
    private ArrayList<ScanFilter> filters;
    private BluetoothGattCharacteristic readCharacteristics, writeCharacteristics;
    private final BluetoothUtilsCallback mUtilsCallback;
    private final String mMacAddr;
    private boolean stopScan = false, bluetoothFound = false, resp = false;

    public BluetoothUtils(Context context, String macAddress, BluetoothUtilsCallback callback) {
        macAddress = "E8:DB:84:1D:A2:C2";
        this.mContext = context;
        this.mMacAddr = macAddress;
        this.mUtilsCallback = callback;
    }

    public boolean bLESupported(){
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    public void startBLE(){
        if (mMacAddr.isEmpty()){
            return;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) mContext.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity)mContext).startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            mBluetoothAdapter.startDiscovery();
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            startScan();
        }
    }

    public void stopScan() {
        if (!stopScan){
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
            stopScan = true;
        }
    }

    /*To be called on Stop*/
    public void closeGatt(){
        stopScan();
        if (mGatt != null) {
            mGatt.close();
            mGatt.disconnect();
            mGatt = null;
        }
    }

    public void write(String data){
        if(mBluetoothAdapter != null && mGatt != null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                //mGatt.beginReliableWrite();
                writeCharacteristics.setValue(data.getBytes());
                if (!mGatt.writeCharacteristic(writeCharacteristics)){
                    Log.e("TAG", "Failed to write characteristics: " + writeCharacteristics.toString());
                }
            }
        }
    }

    private void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mGatt = device.connectGatt(mContext, false, gattCallback, BluetoothDevice.TRANSPORT_LE);
            }
            else{
                mGatt = device.connectGatt(mContext, false, gattCallback);
            }
        }
    }

    private void startScan() {
        if (Build.VERSION.SDK_INT < 21) {
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mLEScanner.startScan(filters, settings, mScanCallback);
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                stopScan();
            }
        }, 20000);
    }

    private final BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    Log.i("onLeScan", device.toString());

                    if (!bluetoothFound && !stopScan){
                        if (device.getAddress() != null &&device.getAddress().equalsIgnoreCase(mMacAddr)){
                            bluetoothFound = true;
                            connectToDevice(device);
                            stopScan();
                        }
                    }
                }
            };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            /*Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());*/
            BluetoothDevice btDevice = result.getDevice();
            String devName = btDevice.getAddress();// result.getScanRecord().getDeviceName();

            if (!bluetoothFound && !stopScan){
                if (devName != null && devName.equalsIgnoreCase(mMacAddr)){
                    bluetoothFound = true;
                    connectToDevice(btDevice);
                }
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                /*
                 * Once successfully connected, we must next discover all the services on the
                 * device before we can read and write their characteristics.
                 */

                gatt.discoverServices();
            } else if (status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                /*
                 * If at any point we disconnect, send a message to clear the weather values
                 * out of the UI
                 */
                gatt.close();
                gatt.disconnect();
                Intent intent = new Intent("init_complete");
                intent.putExtra("status", false);
                LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
            } else if (status != BluetoothGatt.GATT_SUCCESS) {
                /*
                 * If there is a failure at any stage, simply disconnect
                 */
                gatt.close();
                gatt.disconnect();
                Intent intent = new Intent("init_complete");
                intent.putExtra("status", false);
                LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
            }

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            //super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS){

                BluetoothGattService gattService = gatt.getService(SERVICE_UUID);
                if (gattService != null){
                    readCharacteristics = gattService.getCharacteristic(READ_XTC_UUID);

                    if (readCharacteristics != null){
                        /*if(!gatt.setCharacteristicNotification(readCharacteristics, true))
                        {
                            Log.e("TAG", "Failed to set notification for: " + readCharacteristics.toString());
                        }*/
                        gatt.setCharacteristicNotification(readCharacteristics, true);
                        // Enable notification descriptor
                        UUID CCC_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                        BluetoothGattDescriptor descriptor = readCharacteristics.getDescriptor(CCC_UUID);
                        if(descriptor != null) {
                            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                            gatt.writeDescriptor(descriptor);
                        }

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                mUtilsCallback.onConnected();
                            }
                        });

                        Intent intent = new Intent("init_complete");
                        intent.putExtra("status", true);
                        LocalBroadcastManager.getInstance(mContext).sendBroadcastSync(intent);
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        int trials = 5;
                        do{
                            try {
                                mGatt.requestMtu(512);
                                Log.i("TAG", "onTick: "+ resp + " mtu - " + trials);
                                Thread.sleep(100);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        while (!resp && trials-- > 0);
                    }
                }
                stopScan();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic.getValue() != null){
                String value = characteristic.getValue().toString();
                Log.i("TAG", "onCharacteristicRead: " + value);
                mUtilsCallback.onRead(value);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            //super.onCharacteristicWrite(gatt, characteristic, status);
            /*if(characteristic.getValue() != null) {
                if (!gatt.executeReliableWrite()){
                    Log.e("TAG", "Failed to reliable write characteristics: " + characteristic.toString());
                }
            }*/
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            String value = new String(characteristic.getValue());
            Log.i("TAG", "onCharacteristicChanged: " + value);
            mUtilsCallback.onRead(value);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            writeCharacteristics = gatt.getService(SERVICE_UUID).getCharacteristic(WRITE_XTC_UUID);
        }

        @Override
        public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
            super.onReliableWriteCompleted(gatt, status);
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            Log.i("TAG", "onMtuChanged: " + status + ", mtu: " + mtu);
            resp = true;
        }
    };
}
