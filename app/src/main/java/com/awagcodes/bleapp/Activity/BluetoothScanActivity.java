package com.awagcodes.bleapp.Activity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.awagcodes.bleapp.Adapter.DeviceListAdapter;
import com.awagcodes.bleapp.R;
import com.awagcodes.bleapp.Utils.BleScanner;

import java.util.ArrayList;

public class BluetoothScanActivity extends AppCompatActivity implements DeviceListAdapter.DeviceClickInterface {

    private BleScanner scanner;
    private TextView tv_app_bar;
    private RecyclerView recycler_view_devices;
    private ProgressBar progress_cirular;
    private ArrayList<BluetoothDevice> devicesList;
    private static final String TAG = "BluetoothScanActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_scan);
        init();
        scanner.Start();
    }

    private void init() {
        scanner = new BleScanner();
        tv_app_bar = findViewById(R.id.tv_name);
        recycler_view_devices = findViewById(R.id.recycler_view_devices);
        progress_cirular = findViewById(R.id.progress_circular);
        devicesList = new ArrayList<>();

        LiveData<ArrayList<BluetoothDevice>> bluetoothDevices = scanner.getBluetoothState();
        bluetoothDevices.observe(BluetoothScanActivity.this, new Observer<ArrayList<BluetoothDevice>>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(ArrayList<BluetoothDevice> bluetoothDevices) {

                if (bluetoothDevices.size() == 0) {

                    if (devicesList.size()!=0)
                    devicesList.clear();

                    tv_app_bar.setText("Searching For Devices");
                    progress_cirular.setVisibility(View.VISIBLE);
                    recycler_view_devices.setVisibility(View.GONE);

                } else {
                    devicesList = bluetoothDevices;
                    tv_app_bar.setText(devicesList.size() + "  HR Device Found");
                    progress_cirular.setVisibility(View.GONE);
                    recycler_view_devices.setVisibility(View.VISIBLE);
                    SetUpRecyclerView();
                }

                Log.d(TAG, "No of devices found :" + devicesList.size());

            }
        });
    }

    private void SetUpRecyclerView() {
        DeviceListAdapter deviceListAdapter = new DeviceListAdapter(devicesList, BluetoothScanActivity.this);
                recycler_view_devices.setLayoutManager(new LinearLayoutManager(BluetoothScanActivity.this));
                recycler_view_devices.setAdapter(deviceListAdapter);
                recycler_view_devices.setItemAnimator(new DefaultItemAnimator());
                recycler_view_devices.setNestedScrollingEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        scanner.Start();
    }

    @Override
    public void onBackPressed() {
        scanner.Stop();
        super.onBackPressed();
    }

    @Override
    public void onDeviceClickListener(int position) {
        scanner.Stop();
        Intent intent = new Intent(BluetoothScanActivity.this, DashboardActivity.class);
        intent.putExtra("bt_device",devicesList.get(position));
        startActivity(intent);
    }
}
