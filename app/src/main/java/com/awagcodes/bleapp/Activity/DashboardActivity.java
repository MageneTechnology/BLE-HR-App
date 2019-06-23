package com.awagcodes.bleapp.Activity;

import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.awagcodes.bleapp.Adapter.ReadingListAdapter;
import com.awagcodes.bleapp.Utils.BleConnect;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.jobs.MoveViewJob;

import java.util.ArrayList;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.awagcodes.bleapp.R.id;
import static com.awagcodes.bleapp.R.layout;

public class DashboardActivity extends AppCompatActivity implements ReadingListAdapter.DeleteReadingInterface, ReadingListAdapter.SelectReadingInterface, View.OnClickListener {

    private BleConnect bleConnect;
    private BluetoothDevice device;
    private int flagReading;
    private int flagLocation;
    private ArrayList<Integer> readingList;
    private ArrayList<String> locationList;
    private ArrayList<Entry> entryArrayList;
    private static final String TAG = "DashboardActivity";

    private ImageView img_app_bar;
    private TextView tv_name_device;
    private TextView tv_address_device;
    private TextView tv_name;
    private TextView tv_address;
    private CardView cv_unpair_device;
    private TextView tv_connecting;
    private ProgressBar progress_bar;
    private LineChart line_chart_hr;
    private ImageView img_placeholder;
    private TextView tv_placeholder;
    private ReadingListAdapter readingListAdapter;
    private RecyclerView recycler_view_reading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_dashboard);
        Intent intent = getIntent();

        flagReading = -1;
        flagLocation = -1;

        if(intent.getExtras()!=null) {
            device = intent.getExtras().getParcelable("bt_device");
        }

        bleConnect = new BleConnect(DashboardActivity.this,device);
        bleConnect.Connect();

        init();
    }

    private void init() {

        img_app_bar = findViewById(id.img_app_bar);
        tv_name_device = findViewById(id.tv_name_device);
        tv_address_device = findViewById(id.tv_address_device);
        cv_unpair_device = findViewById(id.cv_unpair_device);
        cv_unpair_device.setOnClickListener(this);
        tv_connecting = findViewById(id.tv_connecting);
        progress_bar = findViewById(id.progress_bar);
        line_chart_hr = findViewById(id.line_chart_hr);
        img_placeholder = findViewById(id.img_placeholder);
        tv_placeholder = findViewById(id.tv_placeholder);
        recycler_view_reading = findViewById(id.recycler_view_reading);
        tv_name = findViewById(id.tv_name);
        tv_address = findViewById(id.tv_address);
        readingList = new ArrayList<>();
        locationList = new ArrayList<>();
        entryArrayList = new ArrayList<>();

        initLiveDataObservers();

    }

    private void initLiveDataObservers() {
        LiveData<String> connectionState = bleConnect.getConnectionState();
        connectionState.observe(DashboardActivity.this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                switch (s){
                    case "STATE_CONNECTED":
                        setAppBar(true);
                        setPlaceHolder(true);
                        Toast.makeText(DashboardActivity.this,"Device Connected",Toast.LENGTH_SHORT).show();
                        break;
                    case "STATE_CONNECTING":
                        break;
                    case "STATE_DISCONNECTED":
                        setAppBar(false);
                        setPlaceHolder(false);
                        break;
                }
            }
        });

        LiveData<Integer> readingLiveData = bleConnect.getReadingLiveData();
        readingLiveData.observe(DashboardActivity.this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integers) {
                readingList.add(integers);
                flagReading+=1;
                if(flagReading == flagLocation){
                    Log.d(TAG,"Reading : "+readingList.get(flagReading)+" Location: "+locationList.get(flagLocation));
                    Toast.makeText(DashboardActivity.this,"Reading | "+readingList.get(flagReading)+" Location | "+locationList.get(flagLocation),Toast.LENGTH_SHORT).show();
                    entryArrayList.add(new Entry(flagReading,readingList.get(flagReading)));
                    DrawLineChart();
                    if(readingList.size()==0){
                        SetUpRecyclerView();
                    }
                    else {
                        readingListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        LiveData<String> locationLiveData = bleConnect.getLocationLiveData();
        locationLiveData.observe(DashboardActivity.this, new Observer<String>() {
            @Override
            public void onChanged(String strings) {
                locationList.add(strings);
                flagLocation+=1;
                if(flagReading == flagLocation){
                    Log.d(TAG,"Reading : "+readingList.get(flagReading)+" Location: "+locationList.get(flagLocation));
                    Toast.makeText(DashboardActivity.this,"Heart Rate : "+readingList.get(flagReading)+", Device Location : "+locationList.get(flagLocation),Toast.LENGTH_SHORT).show();
                    entryArrayList.add(new Entry(flagReading,readingList.get(flagReading)));
                    DrawLineChart();
                    if(locationList.size()==1){
                        SetUpRecyclerView();

                        recycler_view_reading.setVisibility(VISIBLE);
                        tv_placeholder.setVisibility(GONE);
                        img_placeholder.setVisibility(GONE);
                    }
                    else {
                        readingListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void DrawLineChart() {

        if (entryArrayList.size()==0){
            line_chart_hr.setVisibility(GONE);
        }else {

        line_chart_hr.setVisibility(VISIBLE);
        LineDataSet set1;
        if (line_chart_hr.getData() != null &&
                line_chart_hr.getData().getDataSetCount() > 0) {
            set1 = (LineDataSet) line_chart_hr.getData().getDataSetByIndex(0);
            set1.setValues(entryArrayList);
            line_chart_hr.getData().notifyDataChanged();
            line_chart_hr.notifyDataSetChanged();
            line_chart_hr.invalidate();
        } else {

            line_chart_hr.setDrawGridBackground(false);
            line_chart_hr.setDrawBorders(false);
            line_chart_hr.setAutoScaleMinMaxEnabled(true);

            Legend legend = line_chart_hr.getLegend();
            legend.setEnabled(false);
            Description description = line_chart_hr.getDescription();
            description.setEnabled(false);

            line_chart_hr.moveViewToX(0);
            line_chart_hr.setVisibleXRangeMaximum(5);

            YAxis leftAxis = line_chart_hr.getAxisLeft();
            leftAxis.setEnabled(false);
            YAxis rightAxis = line_chart_hr.getAxisRight();
            rightAxis.setEnabled(false);

            XAxis xAxis = line_chart_hr.getXAxis();
            xAxis.setEnabled(true);
            xAxis.setTextColor(Color.WHITE);

            set1 = new LineDataSet(entryArrayList, "");
            set1.setDrawIcons(false);
            set1.setColor(Color.WHITE);
            set1.setCircleColor(Color.WHITE);
            set1.setLineWidth(3f);
            set1.setCircleRadius(5f);
            set1.setDrawCircleHole(true);
            set1.setValueTextSize(9f);
            set1.setValueTextColor(Color.CYAN);
            set1.setDrawFilled(true);
            set1.setFillColor(Color.LTGRAY);
            set1.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
            set1.setFormLineWidth(1f);
            set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
            set1.setFormSize(15.f);
            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1);
            LineData data = new LineData(dataSets);
            line_chart_hr.setData(data);
            line_chart_hr.invalidate();
            }
        }
    }

    private void setAppBar(Boolean set) {
        if(set) {
            tv_connecting.setVisibility(GONE);
            img_app_bar.setVisibility(VISIBLE);
            tv_name.setVisibility(VISIBLE);
            tv_address.setVisibility(VISIBLE);
            tv_name_device.setText(device.getName());
            tv_name_device.setVisibility(VISIBLE);
            tv_address_device.setText(device.getAddress());
            tv_address_device.setVisibility(VISIBLE);
            cv_unpair_device.setVisibility(VISIBLE);
            progress_bar.setVisibility(GONE);
        }else {
            tv_connecting.setVisibility(VISIBLE);
            img_app_bar.setVisibility(GONE);
            tv_name.setVisibility(GONE);
            tv_address.setVisibility(GONE);
            tv_name_device.setVisibility(GONE);
            tv_address_device.setVisibility(GONE);
            cv_unpair_device.setVisibility(GONE);
            progress_bar.setVisibility(VISIBLE);
        }
    }

    private void setPlaceHolder(Boolean set) {
        if(set){
            if(readingList.size() == 0 && locationList.size() == 0){
                img_placeholder.setVisibility(VISIBLE);
                tv_placeholder.setVisibility(VISIBLE);
            }else {
                img_placeholder.setVisibility(GONE);
                tv_placeholder.setVisibility(GONE);
            }
        }else {
            img_placeholder.setVisibility(GONE);
            tv_placeholder.setVisibility(GONE);
        }
    }

    private void SetUpRecyclerView() {
        readingListAdapter = new ReadingListAdapter(
                readingList,locationList,DashboardActivity.this,DashboardActivity.this);
        recycler_view_reading.setLayoutManager(new LinearLayoutManager(DashboardActivity.this));
        recycler_view_reading.setAdapter(readingListAdapter);
        recycler_view_reading.setItemAnimator(new DefaultItemAnimator());
        recycler_view_reading.setNestedScrollingEnabled(true);
    }

    @Override
    public void onDeleteClickListener(int position) {
        Toast.makeText(DashboardActivity.this,"Delete position :"+position,Toast.LENGTH_SHORT).show();
        readingList.remove(position);
        locationList.remove(position);
        entryArrayList.remove(position);

        flagReading-=1;
        flagLocation-=1;

        if(readingList.size() == 0){
            img_placeholder.setVisibility(VISIBLE);
            tv_placeholder.setVisibility(VISIBLE);
            recycler_view_reading.setVisibility(GONE);
        }

        readingListAdapter.notifyDataSetChanged();
        DrawLineChart();
    }

    @Override
    public void onViewClickListener(int position) {
        Toast.makeText(DashboardActivity.this,"Heart Rate : "+readingList.get(position)+", Device Location : "+locationList.get(position),Toast.LENGTH_SHORT).show();
        line_chart_hr.highlightValue(entryArrayList.get(position).getX(),entryArrayList.get(position).getY(),position);
    }

    @Override
    public void onBackPressed() {
        disconnectDevice();
    }

    private void disconnectDevice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(DashboardActivity.this)
                .setTitle("End Session with "+device.getName()+" ?")
                .setCancelable(true)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        // To avoid memory leak from mp chart
                        MoveViewJob.getInstance(null,0,0,null,null);

                        bleConnect.Disconnect();
                        finish();
                    }
                });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == id.cv_unpair_device){
            disconnectDevice();
        }
    }
}
