package com.awagcodes.bleapp.Activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.awagcodes.bleapp.BroadcastReciever.StateChangeReceiver;
import com.awagcodes.bleapp.R;

public class BluetoothCheckActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String TAG = "BluetoothCheckActivity";
    private BluetoothAdapter bluetoothAdapter;
    private StateChangeReceiver stateChangeReceiver;
    private Button btn_start_bt;
    private Button btn_start_scan;
    private ImageView img_bt_state;
    private TextView tv_bt_state;
    static final Integer MY_PERMISSIONS_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_check);
        init();
        checkBluetoothState();
    }

    private void init() {
        btn_start_bt = findViewById(R.id.btn_start_bt);
        img_bt_state = findViewById(R.id.img_bt_state);
        tv_bt_state = findViewById(R.id.tv_bt_state);
        btn_start_scan = findViewById(R.id.btn_start_scan);

        btn_start_bt.setOnClickListener(this);
        btn_start_scan.setOnClickListener(this);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        stateChangeReceiver = new StateChangeReceiver();

        LiveData<String> bluetoothState = stateChangeReceiver.getBluetoothState();
        bluetoothState.observe(BluetoothCheckActivity.this, new Observer<String>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(String s) {
                switch (s) {
                    case "STATE OFF":
//                        Log.d(TAG, "STATE_OFF");
//                        btn_start_bt.setVisibility(View.VISIBLE);
//                        btn_start_scan.setVisibility(View.GONE);
//                        img_bt_state.setImageResource(R.mipmap.ic_bt_off);
//                        tv_bt_state.setText("disabled.");
                        break;
                    case "STATE_TURNING_OFF":
                        Log.d(TAG, "STATE_TURNING_OFF");
                        break;
                    case "STATE_ON":
                        Log.d(TAG, "STATE_ON");
                        Log.d(TAG, "GOTO NEXT ACTIVITY");
                        btn_start_bt.setVisibility(View.GONE);
                        btn_start_scan.setVisibility(View.VISIBLE);
                        img_bt_state.setImageResource(R.mipmap.ic_bt_on);
                        tv_bt_state.setText("enabled.");
                        break;
                    case "TURNING_ON":
                        Log.d(TAG, "TURNING_ON");
                        break;

                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_start_bt) {
            enableBluetooth();
        } else if (v.getId() == R.id.btn_start_scan) {
            goToScanActivity();
        }
    }

    private void goToScanActivity() {
        if (ContextCompat.checkSelfPermission(BluetoothCheckActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            checkBTPermission();
        }else {
            makeTransition();
        }
    }

    private void checkBTPermission() {

        if (ContextCompat.checkSelfPermission(BluetoothCheckActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(BluetoothCheckActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {

                ActivityCompat.requestPermissions(BluetoothCheckActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST);

            } else {

                ActivityCompat.requestPermissions(BluetoothCheckActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        MY_PERMISSIONS_REQUEST);

            }
        } else {
            makeTransition();
            // Permission has already been granted
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makeTransition();
            } else {

                Toast.makeText(BluetoothCheckActivity.this, "Please grant permission from settings!", Toast.LENGTH_SHORT).show();

            }
        }
    }

    private void enableBluetooth() {
        Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivity(enableBTIntent);
    }

    @SuppressLint("SetTextI18n")
    private void checkBluetoothState() {

        IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(stateChangeReceiver, BTIntent);

        if (bluetoothAdapter == null) {
            Toast.makeText(BluetoothCheckActivity.this, "Device Does Not Have Bluetooth Ability", Toast.LENGTH_SHORT).show();
        } else if (bluetoothAdapter.isEnabled()) {
            btn_start_bt.setVisibility(View.GONE);
            btn_start_scan.setVisibility(View.VISIBLE);
            img_bt_state.setImageResource(R.mipmap.ic_bt_on);
            tv_bt_state.setText("enabled.");
        }
    }



    private void makeTransition() {
        Intent intent = new Intent(BluetoothCheckActivity.this, BluetoothScanActivity.class);
        startActivity(intent);
        finish();
    }

    // So the broadcast receiver is stopped as it is very memory intensive
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(stateChangeReceiver);
    }

}
