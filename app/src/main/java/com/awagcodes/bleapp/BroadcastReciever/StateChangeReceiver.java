package com.awagcodes.bleapp.BroadcastReciever;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

public class StateChangeReceiver extends BroadcastReceiver {

    private static final String TAG = "StateChangeReceiver";
    private MutableLiveData<String> bluetoothState;

    public MutableLiveData<String> getBluetoothState() {
        if (bluetoothState == null) {
            bluetoothState = new MutableLiveData<>();
        }
        return bluetoothState;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        assert action != null;
        if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {

            final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    Log.d(TAG, "STATE OFF");
                    bluetoothState.setValue("STATE_OFF");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    Log.d(TAG, "TURNING OFF");
                    bluetoothState.setValue("TURNING_OFF");
                    break;
                case BluetoothAdapter.STATE_ON:
                    Log.d(TAG, "STATE ON");
                    bluetoothState.setValue("STATE_ON");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    Log.d(TAG, "TURNING ON");
                    bluetoothState.setValue("TURNING_ON");
                    break;
            }

        } else {
            Log.d(TAG, "Wrong Receiver Used");
        }
    }
}
