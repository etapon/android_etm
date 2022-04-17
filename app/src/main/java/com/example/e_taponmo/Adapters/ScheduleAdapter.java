package com.example.e_taponmo.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.e_taponmo.CityActivity;
import com.example.e_taponmo.Models.Schedule;
import com.example.e_taponmo.R;
import com.example.e_taponmo.constants;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleHolder> {

    private ArrayList<Schedule> list;
    private Context context;

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://e-tapon-mo-default-rtdb.firebaseio.com/");
    private DatabaseReference firebaseActiveStatus = database.getReference("activeStatus");
    private DatabaseReference wasteType = database.getReference("wasteType");
    private String scheduleId;
    private SharedPreferences sharedPreferences;
    private ArrayList<String> streetList;
    private String activeStatusValue;
//    private Queue streetQueue = new LinkedList<>();


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

//        Toast.makeText(context, ""+scheduleId, Toast.LENGTH_SHORT).show();
        scheduleId = schedule.getId();

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
            activeStatus.setColor(context.getResources().getColor(R.color.color_idle));
            holder.txtScheduleActiveStatus.setText("day of collection");
        }else{
            activeStatus.setColor(context.getResources().getColor(R.color.color_offline));
            holder.txtScheduleActiveStatus.setText("offline");
        }

        holder.layoutSchedule.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(str.equals(schedule.day)){
                    firebaseActiveStatus.setValue("active");
                    wasteType.setValue(schedule.getTypeOfWaste());
                    getStreets();
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

    public void getStreets(){
        sharedPreferences = context.getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        Queue<String> streetQueue = new LinkedList<String>();
        streetList = new ArrayList<>();
        Bundle bundle = new Bundle();

        StringRequest request = new StringRequest(Request.Method.GET, constants.SCHEDULE+scheduleId, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if(object.getBoolean("success")){
                    JSONObject result = object.getJSONObject("result");

                    JSONArray streets = result.getJSONArray("queue");

                    for(int i2 = 0; i2 < streets.length(); i2++){
                        System.out.println(streets.get(i2));
                        streetList.add(streets.get(i2).toString());
                    }
                }
                Intent intent = new Intent(context, CityActivity.class);
                intent.putExtra("streetQueue", streetList);
                context.startActivity(intent);


            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            error.printStackTrace();
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token", "");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization", "Bearer "+token);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

}
