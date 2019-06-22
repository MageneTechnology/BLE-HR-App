package com.awagcodes.bleapp.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.awagcodes.bleapp.R;

import java.util.ArrayList;

public class ReadingListAdapter extends RecyclerView.Adapter<ReadingListAdapter.ReadingViewHolder> {

    private ArrayList<Integer> readingList;
    private ArrayList<String> locationList;
    private DeleteReadingInterface deleteReadingInterface;
    private SelectReadingInterface selectReadingInterface;

    public ReadingListAdapter(ArrayList<Integer> readingList, ArrayList<String> locationList,DeleteReadingInterface deleteReadingInterface,SelectReadingInterface selectReadingInterface) {
        this.readingList = readingList;
        this.locationList = locationList;
        this.deleteReadingInterface = deleteReadingInterface;
        this.selectReadingInterface = selectReadingInterface;
    }

    @NonNull
    @Override
    public ReadingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reading_list_item,parent,false);
        return new ReadingViewHolder(view,deleteReadingInterface,selectReadingInterface);
    }

    @Override
    public void onBindViewHolder(@NonNull ReadingViewHolder holder, int position) {
        String heartRate = Integer.toString(readingList.get(position));
        String deviceSensorLocation = locationList.get(position);

        holder.tv_hr_reading.setText(heartRate);
        holder.tv_hr_location.setText(deviceSensorLocation);
    }

    @Override
    public int getItemCount() {
        return readingList.size();
    }

    public interface DeleteReadingInterface{
        void onDeleteClickListener(int position);
    }
    public interface SelectReadingInterface{
        void onViewClickListener(int position);
    }

    public class ReadingViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView tv_hr_reading;
        private TextView tv_hr_location;
        CardView cv_remove_reading;
        CardView cv_reading;
        DeleteReadingInterface deleteReadingInterface;
        SelectReadingInterface selectReadingInterface;

         ReadingViewHolder(@NonNull View itemView, DeleteReadingInterface deleteReadingInterface,SelectReadingInterface selectReadingInterface) {
            super(itemView);

            tv_hr_reading = itemView.findViewById(R.id.tv_hr_reading);
            tv_hr_location = itemView.findViewById(R.id.tv_hr_location);
            cv_remove_reading = itemView.findViewById(R.id.cv_remove_reading);
            cv_reading = itemView.findViewById(R.id.cv_reading);
            cv_reading.setOnClickListener(this);
            cv_remove_reading.setOnClickListener(this);
            this.deleteReadingInterface = deleteReadingInterface;
            this.selectReadingInterface = selectReadingInterface;
        }

        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.cv_remove_reading) {
                deleteReadingInterface.onDeleteClickListener(getAdapterPosition());
            }else if(v.getId() == R.id.cv_reading){
                selectReadingInterface.onViewClickListener(getAdapterPosition());
            }
        }
    }

}
