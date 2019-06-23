package com.awagcodes.bleapp.Utils;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.lang.ref.WeakReference;

import static android.bluetooth.BluetoothProfile.STATE_CONNECTED;
import static android.bluetooth.BluetoothProfile.STATE_CONNECTING;
import static android.bluetooth.BluetoothProfile.STATE_DISCONNECTED;

public class BleConnect {

    private BluetoothGatt bluetoothGatt;
    private WeakReference<Context> contextReference;

    private BluetoothDevice device;
    private BluetoothGattCallback gattCallback;
    private BluetoothGattDescriptor descriptorHr;
    private BluetoothGattCharacteristic characteristicHr;
    private BluetoothGattCharacteristic characteristicLoc;

    private MutableLiveData<String> connectionState;
    private MutableLiveData<Integer> readingLiveData;
    private MutableLiveData<String> locationLiveData;

    private static final String TAG = "BleConnect";

    public BleConnect(Context context,BluetoothDevice device){
        contextReference = new WeakReference<>(context);

        this.device = device;
        connectionState = new MutableLiveData<>();
        readingLiveData = new MutableLiveData<>();
        locationLiveData = new MutableLiveData<>();
    }

    public MutableLiveData<String> getConnectionState(){

        return connectionState;
    }

    public MutableLiveData<Integer> getReadingLiveData(){

        return readingLiveData;
    }

    public MutableLiveData<String> getLocationLiveData(){

        return locationLiveData;
    }

    public void Connect(){

        gattCallback = new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if(newState == STATE_CONNECTED){
                    bluetoothGatt.discoverServices();
                    Log.d(TAG,"Device Connected!");
                    connectionState.postValue("STATE_CONNECTED");
                } else if (newState == STATE_CONNECTING){
                    Log.d(TAG,"Device Connecting...");
                    connectionState.postValue("STATE_CONNECTING");
                } else if (newState == STATE_DISCONNECTED){
                    Log.d(TAG,"Device Disconnected!");

                    bluetoothGatt = device.connectGatt(contextReference.get(),false,gattCallback);
                    connectionState.postValue("STATE_DISCONNECTED");
                }
            }

            // Discover Services Call Back Method

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {

                if(status == BluetoothGatt.GATT_SUCCESS) {

                    // call for the HR reading
                    characteristicHr = gatt.getService(AppConstants.HEART_RATE_SERVICE_UUID).getCharacteristic(AppConstants.HEART_RATE_MEASUREMENT_UUID);
                    gatt.setCharacteristicNotification(characteristicHr, true);
                    descriptorHr = characteristicHr.getDescriptor(AppConstants.CLIENT_CHARACTERISTIC_CONFIG_UUID);
                    descriptorHr.setValue(
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                    gatt.writeDescriptor(descriptorHr);

                }
            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                if (descriptor.equals(descriptorHr)) {
                    gatt.readCharacteristic(characteristicHr);
                }else {
                    gatt.readCharacteristic(characteristicLoc);
                }
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                readCounterCharacteristic(characteristic);
            }

            private void readCounterCharacteristic(BluetoothGattCharacteristic characteristic) {
                if (AppConstants.HEART_RATE_MEASUREMENT_UUID.equals(characteristic.getUuid())) {

                    int flag = characteristic.getProperties();
                    int format;
                    if ((flag & 0x01) != 0) {
                        format = BluetoothGattCharacteristic.FORMAT_UINT16;
                        Log.d(TAG, "Heart rate format UINT16.");
                    } else {
                        format = BluetoothGattCharacteristic.FORMAT_UINT8;
                        Log.d(TAG, "Heart rate format UINT8.");
                    }
                    final int heartRate = characteristic.getIntValue(format, 1);
                    Log.d(TAG, String.format("Received heart rate: %d", heartRate));

                    readingLiveData.postValue(heartRate);

                }else if (AppConstants.BODY_SENSOR_LOCATION_UUID.equals(characteristic.getUuid())){
                    // For all other profiles, writes the data formatted in HEX.
                    final byte[] data = characteristic.getValue();
                    if (data != null && data.length > 0) {
                        final StringBuilder stringBuilder = new StringBuilder(data.length);
                        for(byte byteChar : data)
                            stringBuilder.append(String.format("%02X ", byteChar));
                            switch (Integer.parseInt(stringBuilder.toString().trim())){
                                case 0:
                                    locationLiveData.postValue("Other");
                                    Log.d(TAG,"Location Other");
                                    break;
                                case 1:
                                    locationLiveData.postValue("Chest");
                                    Log.d(TAG,"Location Chest");
                                    break;
                                case 2:
                                    locationLiveData.postValue("Wrist");
                                    Log.d(TAG,"Location Wrist");
                                    break;
                                case 3:
                                    locationLiveData.postValue("Finger");
                                    Log.d(TAG,"Location Finger");
                                    break;
                                case 4:
                                    locationLiveData.postValue("Hand");
                                    Log.d(TAG,"Location Hand");
                                    break;
                                case 5:
                                    locationLiveData.postValue("Ear Lobe");
                                    Log.d(TAG,"Location Ear Lobe");
                                    break;
                                case 6:
                                    locationLiveData.postValue("Foot");
                                    Log.d(TAG,"Location Foot");
                                    break;
                            }
                    }
                }
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                readCounterCharacteristic(characteristic);

                // call for the body sensor location
                characteristicLoc = gatt.getService(AppConstants.HEART_RATE_SERVICE_UUID).getCharacteristic(AppConstants.BODY_SENSOR_LOCATION_UUID);
                if(characteristicLoc == null){
                    Log.d(TAG,"It is null");
                }
                gatt.setCharacteristicNotification(characteristicLoc, true);
                gatt.readCharacteristic(characteristicLoc);
            }
        };

        bluetoothGatt = device.connectGatt(contextReference.get(), false, gattCallback);
    }

    public void Disconnect(){
        bluetoothGatt.disconnect();
    }

}
