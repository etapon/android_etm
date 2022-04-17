package com.example.e_taponmo.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.e_taponmo.Adapters.ScheduleAdapter;
import com.example.e_taponmo.CollectorsActivity;
import com.example.e_taponmo.Models.Schedule;
import com.example.e_taponmo.Models.Street;
import com.example.e_taponmo.R;
import com.example.e_taponmo.constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScheduleFragment extends Fragment {
    private View view;
    private RecyclerView recyclerView;
    private ArrayList<Schedule> arrayList;
    private SwipeRefreshLayout refreshLayout;
    private ScheduleAdapter scheduleAdapter;
    private MaterialToolbar toolbar;
    private SharedPreferences sharedPreferences;

    private String collectorsID;

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://e-tapon-mo-default-rtdb.firebaseio.com/");

    private DatabaseReference activeStatus = database.getReference("activeStatus");


    public ScheduleFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_schedules, container, false);
        init();
        return view;
    }

    private void init(){
        sharedPreferences = getContext().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        collectorsID = sharedPreferences.getString("id", "");

        recyclerView = view.findViewById(R.id.recyclerSchedule);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayout = view.findViewById(R.id.swipeSchedules);
        toolbar = view.findViewById(R.id.toolBarSchedule);
        ((CollectorsActivity)getContext()).setSupportActionBar(toolbar);

        getSchedules();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSchedules();
            }
        });

        activeStatus.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                if(value.equals("idle")){
//                    activeStatus.setImageResource(android.drawable.white_play);
                } else {

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("debug", "Failed to read value.", error.toException());
            }
        });
    }

    private void getSchedules(){
        arrayList = new ArrayList<>();
        refreshLayout.setRefreshing(true);

        StringRequest request = new StringRequest(Request.Method.GET, constants.SCHEDULES+collectorsID, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if(object.getBoolean("success")){
                    JSONArray array = new JSONArray(object.getString("result"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject scheduleObject = array.getJSONObject(i);
                        Schedule schedule = new Schedule();

                        schedule.setId(scheduleObject.getString("_id"));
                        schedule.setDay(scheduleObject.getString("day"));
                        schedule.setTypeOfWaste(scheduleObject.getString("type"));
                        schedule.setStartOfCollection(scheduleObject.getString("startOfCollection"));

                        JSONArray streets = scheduleObject.getJSONArray("queue");
                        ArrayList<String> listdata = new ArrayList<String>();

                        for(int i2 = 0; i2 < streets.length(); i2++){
                            schedule.addToQueue(streets.get(i2).toString());
                        }

                        arrayList.add(schedule);

                    }
                    scheduleAdapter = new ScheduleAdapter(getContext(), arrayList);
                    recyclerView.setAdapter(scheduleAdapter);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            refreshLayout.setRefreshing(false);
        }, error -> {
            error.printStackTrace();
            refreshLayout.setRefreshing(false);
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token", "");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization", "Bearer "+token);
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }

}
