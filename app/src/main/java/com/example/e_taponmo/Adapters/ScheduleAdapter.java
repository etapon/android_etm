package com.example.e_taponmo.Adapters;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_taponmo.CityActivity;
import com.example.e_taponmo.CollectorsActivity;
import com.example.e_taponmo.Fragments.AnnouncementFragment;
import com.example.e_taponmo.Fragments.StreetFragment;
import com.example.e_taponmo.HomeActivity;
import com.example.e_taponmo.Models.Announcement;
import com.example.e_taponmo.Models.Schedule;
import com.example.e_taponmo.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleHolder>{

    private ArrayList<Schedule> list;
    private Context context;

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://e-tapon-mo-default-rtdb.firebaseio.com/");
    private DatabaseReference streetAssigned = database.getReference("streetAssigned");
    private DatabaseReference wasteType = database.getReference("wasteType");

    public ScheduleAdapter(Context context, ArrayList<Schedule> list) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ScheduleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_schedule, parent, false);
        return new ScheduleHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleHolder holder, int position) {
        Schedule schedule = list.get(position);

        holder.txtScheduleDay.setText("   "+schedule.getDay());
        holder.txtScheduleType.setText("type: "+schedule.getTypeOfWaste());
        holder.txtScheduleStart.setText("start of collection: "+schedule.getStartOfCollection());

        GradientDrawable activeStatus = (GradientDrawable) holder.scheduleActiveStatus.getBackground();



        if(schedule.getTypeOfWaste().equals("Biodegradable")){
            holder.scheduleImageType.setBackground(ContextCompat.getDrawable(context,R.drawable.bio_logo));
        }else if(schedule.getTypeOfWaste().equals("non-Biodegradable")){
            holder.scheduleImageType.setBackground(ContextCompat.getDrawable(context,R.drawable.non_bio_logo));
        }else{
            holder.scheduleImageType.setBackground(ContextCompat.getDrawable(context,R.drawable.recyclable_logo));
        }

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Format f = new SimpleDateFormat("EEEE");
        String str = f.format(new Date());

        if(str.equals(schedule.day)){
            activeStatus.setColor(context.getResources().getColor(R.color.color_active));
            holder.txtScheduleActiveStatus.setText("active");
        }else{
            activeStatus.setColor(context.getResources().getColor(R.color.color_offline));
            holder.txtScheduleActiveStatus.setText("offline");
        }

        holder.layoutSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(str.equals(schedule.day)){
                    wasteType.setValue(schedule.getTypeOfWaste());
                    StreetFragment streetFragment = new StreetFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("id", schedule.getId());
                    streetFragment.setArguments(bundle);
                    ((CollectorsActivity)context).getSupportFragmentManager().beginTransaction().replace(R.id.collectors_frame_layout, streetFragment).addToBackStack(null).commit();
                } else {
                    Toast.makeText(context, "this schedule is for "+schedule.getDay()+" only", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ScheduleHolder extends RecyclerView.ViewHolder{

        private TextView txtScheduleDay, txtScheduleType, txtScheduleStart, txtScheduleActiveStatus;
        private LinearLayout scheduleActiveStatus, scheduleImageType, layoutSchedule;

        public ScheduleHolder(@NonNull View itemView){
            super(itemView);

            txtScheduleDay = itemView.findViewById(R.id.txtScheduleDay);
            txtScheduleType = itemView.findViewById(R.id.txtScheduleType);
            txtScheduleStart = itemView.findViewById(R.id.txtScheduleStart);
            txtScheduleActiveStatus = itemView.findViewById(R.id.txtScheduleActiveStatus);

            scheduleActiveStatus = itemView.findViewById(R.id.scheduleActiveStatus);
            scheduleImageType = itemView.findViewById(R.id.scheduleImageType);
            layoutSchedule = itemView.findViewById(R.id.layoutSchedule);

        }

    }

}
