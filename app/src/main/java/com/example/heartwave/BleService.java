package com.example.heartwave;

import android.app.Dialog;
import android.app.Service;
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
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BleService extends Service {
    Toast toast;
    private final IBinder binder = new LocalBinder();
    ListView mPairedList;
    BluetoothManager bluetoothManager;
    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    ArrayList<BluetoothDevice> bluetoothDevices;
    ArrayList<String> deviceList;
    List<ScanFilter> filtersBle;
    ScanSettings scanSettings;
    BluetoothGatt bluetoothGatt;
    Boolean isConnected;
    BluetoothDevice connectedDevice;
    Boolean isNotified;
    byte lowByte, highByte;
    int levelADC;
    //static byte[] buffer;
    static double voltageADC;
    Dialog popup;
    /*  <Debugging      */
    int counter = 0;
    int valid = 0;
    Long startTimestamp;
    Long currentTimestamp;
    Long difference;
    /*  Debugging />    */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onCreate() {
        super.onCreate();
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        bluetoothDevices = new ArrayList<>();
        deviceList = new ArrayList<>();
        filtersBle = new ArrayList<>();
        scanSettings = new ScanSettings.Builder().
                setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        isConnected = false;
        isNotified = false;

        final String[] addresses = {
                "04:91:62:9F:94:83",
                "E0:7D:EA:40:A0:82",
                "04:91:62:A4:CF:2D",
                "A4:DA:32:52:20:6E"
        };

        for(String address : addresses) {
            ScanFilter filter = new ScanFilter.Builder()
                    .setDeviceAddress(address)
                    .build();
            filtersBle.add(filter);
        }

        if(bluetoothAdapter != null) {
            bluetoothAdapter.enable();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        BleService getService() {
            return BleService.this;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void scanBleDevices(ListView scanList) {
        this.mPairedList = scanList;
        Handler handler = new Handler();
        final long SCAN_PERIOD = 15000;
        bluetoothDevices.clear();
        deviceList.clear();
        bluetoothLeScanner.stopScan(leScanCallback);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                bluetoothLeScanner.stopScan(leScanCallback);
            }
        }, SCAN_PERIOD);
        showToast("SCANNING");
        bluetoothLeScanner.startScan(filtersBle, scanSettings, leScanCallback);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            if (!bluetoothDevices.contains(result.getDevice())) {
                final BleAdvertisedData data = BleUtil.parseAdertisedData(Objects.requireNonNull(result.getScanRecord()).getBytes());
                String deviceName = result.getDevice().getName();
                if (deviceName == null) {
                    deviceName = data.getName();
                }
                bluetoothDevices.add(result.getDevice());
                deviceList.add(deviceName + "\n" + result.getDevice().getAddress() +
                        "\n" + result.getRssi() + " dBm");

                mPairedList.setAdapter(new ArrayAdapter<>
                        (getApplicationContext(), android.R.layout.simple_list_item_1, deviceList));
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void connectToGatt(BluetoothDevice device) {
        if(isConnected) {
            showToast("Device is already connected");
        }
        else {
            connectedDevice = device;
            bluetoothLeScanner.stopScan(leScanCallback);
            bluetoothGatt = device.connectGatt(this, false, gattCallback);
            showToast("Connecting...");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void disconnectFromGatt() {
        if(isConnected) {
            bluetoothGatt.disconnect();
        }
        else {
            showToast("Device is not connected");
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
            @Override
            public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyUpdate(gatt, txPhy, rxPhy, status);
            }
            @Override
            public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
                super.onPhyRead(gatt, txPhy, rxPhy, status);
            }
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);

                if(newState == BluetoothProfile.STATE_CONNECTED) {
                    switch(status) {
                        case BluetoothGatt.GATT_SUCCESS:
                            isConnected = true;
                            showToast("Connected");
                            gatt.discoverServices();
                            break;
                        case BluetoothGatt.GATT_FAILURE:
                            isConnected = true;
                            showToast("Failed to connect");
                            break;
                        default:
                            isConnected = true;
                            showToast("Error connecting");
                    }
                }
                else if(newState == BluetoothProfile.STATE_DISCONNECTED) {
                    switch(status) {
                        case BluetoothGatt.GATT_SUCCESS:
                            isConnected = false;
                            isNotified = false;
                            showToast("Disconnected");
                            break;
                        case BluetoothGatt.GATT_FAILURE:
                            isConnected = false;
                            isNotified = false;
                            showToast("Failed to disconnect");
                            break;
                        default:
                            isConnected = false;
                            isNotified = false;
                            showToast("Error: disconnecting");
                    }
                }
            }
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);

                final UUID SERVICE_UUID;
                final UUID CHARACTERISTIC_UUID;
                final UUID DESCRIPTOR_UUID;

                if(connectedDevice.getAddress().equals("04:91:62:9F:94:83")) {
                    SERVICE_UUID = UUID.fromString("49535343-FE7D-4AE5-8FA9-9FAFD205E455");
                    CHARACTERISTIC_UUID = UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616");
                    DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
                }
                else if(connectedDevice.getAddress().equals("E0:7D:EA:40:A0:82")) {
                    SERVICE_UUID = UUID.fromString("0000FFF0-0000-1000-8000-00805F9B34FB");
                    CHARACTERISTIC_UUID = UUID.fromString("0000FFF4-0000-1000-8000-00805F9B34FB");
                    DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
                }
                else if(connectedDevice.getAddress().equals("04:91:62:A4:CF:2D")) {
                    SERVICE_UUID = UUID.fromString("49535343-FE7D-4AE5-8FA9-9FAFD205E455");
                    CHARACTERISTIC_UUID = UUID.fromString("49535343-1E4D-4BD9-BA61-23C647249616");
                    DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
                }
                else if(connectedDevice.getAddress().equals("A4:DA:32:52:20:6E")) {
                    SERVICE_UUID = UUID.fromString("0000FE0-0000-1000-8000-00805F9B34FB");
                    CHARACTERISTIC_UUID = UUID.fromString("0000FFE1-0000-1000-8000-00805F9B34FB");
                    DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805F9B34FB");
                }
                else
                {
                    SERVICE_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
                    CHARACTERISTIC_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
                    DESCRIPTOR_UUID = UUID.fromString("00000000-0000-0000-0000-000000000000");
                    showToast("Error: cannot receive data");
                }

                if(status == BluetoothGatt.GATT_SUCCESS) {
                    if(!isNotified) {
                        isNotified = true;
                        BluetoothGattService service = gatt.getService(SERVICE_UUID);
                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(CHARACTERISTIC_UUID);
                        gatt.setCharacteristicNotification(characteristic, true);
                        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(DESCRIPTOR_UUID);
                        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                        gatt.writeDescriptor(descriptor);
                    }
                }
            }
            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                byte[] buffer = characteristic.getValue();
                String msg = new String(buffer, 0, buffer.length);
                Log.d("Tag", msg);
            }
            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
            }
            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);

                byte[] buffer = characteristic.getValue();
                if(counter == 0) {
                    startTimestamp = System.currentTimeMillis() / 1000;
                }
                else {
                    currentTimestamp = System.currentTimeMillis() / 1000;
                    difference = currentTimestamp - startTimestamp;
                }

                byte register = buffer[0];
                byte bit7 = (byte) ((register >> 7) & 0x01);
                if(bit7 == 0x00) {
                    /*  Low Byte   */
                    lowByte = (byte) (register & 0x7F);
                }
                else {
                    /*  High Byte    */
                    lowByte |= (register & 0x01);
                    highByte = (byte) ((register >> 1) & 0x03);
                    levelADC = ((highByte & 0xFF) << 8) | (lowByte & 0xFF);
                    voltageADC = levelADC * 3.3 / 1023;
                    String msg = String.valueOf(voltageADC);
                    Log.d("Tag", msg);
                    showToast(msg);
                }

                if(levelADC >= 0 && levelADC < 1024) {
                    valid++;
                    counter++;
                }
                else {
                    counter++;
                }
            }
            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
            }
            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
            }
            @Override
            public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
                super.onReliableWriteCompleted(gatt, status);
            }
            @Override
            public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
                super.onReadRemoteRssi(gatt, rssi, status);
            }
            @Override
            public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
                super.onMtuChanged(gatt, mtu, status);
            }
    };

    public void showToast(final String msg) {
            final Context MyContext = this;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(toast != null) {
                        toast.cancel();
                    }
                    toast = Toast.makeText(MyContext, msg, Toast.LENGTH_SHORT);
                    toast.show();
                }
            });
    }
}