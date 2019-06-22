package com.awagcodes.bleapp.Utils;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static no.nordicsemi.android.support.v18.scanner.ScanSettings.CALLBACK_TYPE_ALL_MATCHES;
import static no.nordicsemi.android.support.v18.scanner.ScanSettings.CALLBACK_TYPE_FIRST_MATCH;
import static no.nordicsemi.android.support.v18.scanner.ScanSettings.SCAN_MODE_BALANCED;

public class BleScanner {

    private BluetoothLeScannerCompat scanner;
    private static final String TAG = "BleScanner";
    private ArrayList<BluetoothDevice> devicesList;
    private MutableLiveData<ArrayList<BluetoothDevice>> bluetoothDevices;

    public BleScanner() {
        scanner = BluetoothLeScannerCompat.getScanner();
        devicesList = new ArrayList<>();
        bluetoothDevices = new MutableLiveData<>();
    }

    public MutableLiveData<ArrayList<BluetoothDevice>> getBluetoothState() {
        if (bluetoothDevices == null) {
            bluetoothDevices = new MutableLiveData<>();
        }
        return bluetoothDevices;
    }

    public void Start(){

        ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setCallbackType(CALLBACK_TYPE_ALL_MATCHES)
                .setScanMode(SCAN_MODE_BALANCED)
                .setReportDelay(5000)
                .setUseHardwareBatchingIfSupported(false)
                .build();

        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(new ParcelUuid(AppConstants.HEART_RATE_SERVICE_UUID)).build());
        scanner.startScan(filters,settings,new ScanCallback() {

            // Callback when a BLE advertisement has been found.
            @Override
            public void onScanResult(int callbackType, @NonNull ScanResult result) {
//                super.onScanResult(callbackType, result);
//                BluetoothDevice device = result.getDevice();
//                devicesList.add(device);
//                bluetoothDevices.setValue(devicesList);
//                Log.d(TAG,"Device found "+device);
            }

            // Callback when batch results are delivered.
            @Override
            public void onBatchScanResults(@NonNull List<ScanResult> results) {
                super.onBatchScanResults(results);
                devicesList.clear();
                Log.d(TAG,"Devices Batch found "+results);
                for (ScanResult result: results){
                    devicesList.add(result.getDevice());
                }
                bluetoothDevices.setValue(devicesList);
            }

            // Callback when scan could not be started.
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Log.d(TAG,"Error found "+errorCode);
            }
        });

    }

    public void Stop(){
        scanner.stopScan(new ScanCallback() {
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        });
    }
}
