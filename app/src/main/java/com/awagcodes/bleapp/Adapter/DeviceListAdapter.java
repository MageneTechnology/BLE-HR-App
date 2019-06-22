package com.awagcodes.bleapp.Adapter;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.awagcodes.bleapp.R;

import java.util.ArrayList;

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DeviceViewHolder> {

    private ArrayList<BluetoothDevice> deviceArrayList;
    private DeviceClickInterface deviceClickInterface;

    public DeviceListAdapter(ArrayList<BluetoothDevice> deviceArrayList, DeviceClickInterface deviceClickInterface) {
        this.deviceArrayList = deviceArrayList;
        this.deviceClickInterface = deviceClickInterface;
    }

    @NonNull
    @Override
    public DeviceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.device_list_item, parent, false);
        return new DeviceViewHolder(view, deviceClickInterface);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull DeviceViewHolder holder, int position) {
        BluetoothDevice device = deviceArrayList.get(position);
        if(device.getName() == null){
            holder.tv_name.setText("Unknown Device");
        }else {
            holder.tv_name.setText(device.getName());
        }
        if(device.getAddress() == null){
            holder.tv_address.setText("NA");
        }else {
            holder.tv_address.setText(device.getAddress());
        }
    }

    @Override
    public int getItemCount() {
        return deviceArrayList.size();
    }

    public interface DeviceClickInterface {
        void onDeviceClickListener(int position);
    }

    public class DeviceViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tv_name;
        private TextView tv_address;
        private CardView cv_device;
        private DeviceClickInterface deviceClickInterface;

        DeviceViewHolder(@NonNull View itemView, DeviceClickInterface deviceClickInterface) {
            super(itemView);

            tv_name = itemView.findViewById(R.id.tv_name);
            tv_address = itemView.findViewById(R.id.tv_address);
            cv_device = itemView.findViewById(R.id.cv_device);
            this.deviceClickInterface = deviceClickInterface;
            cv_device.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.cv_device)
                deviceClickInterface.onDeviceClickListener(getAdapterPosition());
        }
    }

}
