package com.example.e_taponmo.Fragments;

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
import com.example.e_taponmo.Adapters.ScheduleResidentsAdapter;
import com.example.e_taponmo.CityActivity;
import com.example.e_taponmo.CityForResidentsActivity;
import com.example.e_taponmo.CollectorsActivity;
import com.example.e_taponmo.HomeActivity;
import com.example.e_taponmo.Models.Schedule;
import com.example.e_taponmo.Models.ScheduleResidents;
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

import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScheduleForResidentsFragment extends Fragment {

    private View view;
    private RecyclerView recyclerView;
    private ArrayList<ScheduleResidents> arrayList;
    private SwipeRefreshLayout refreshLayout;
    private ScheduleResidentsAdapter scheduleResidentsAdapter;
    private MaterialToolbar toolbar;
    private SharedPreferences sharedPreferences;

    private String residentStreet, role;

    public ScheduleForResidentsFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_schedules_residents, container, false);
        init();
        return view;
    }

    public void init(){
        sharedPreferences = getContext().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        residentStreet = sharedPreferences.getString("street", "");
        role = sharedPreferences.getString("role", "");


        recyclerView = view.findViewById(R.id.recyclerScheduleForResidents);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayout = view.findViewById(R.id.swipeSchedulesForResidents);
        toolbar = view.findViewById(R.id.toolBarScheduleForResidents);
        ((HomeActivity)getContext()).setSupportActionBar(toolbar);

        getSchedule();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getSchedule();
            }
        });

    }

    public void getSchedule(){
        arrayList = new ArrayList<>();
        refreshLayout.setRefreshing(true);

        if(role.equals("admin")){

            StringRequest request = new StringRequest(Request.Method.GET, constants.SCHEDULE_FOR_ADMIN, response -> {
                try{
                    JSONObject object = new JSONObject(response);
                    if(object.getBoolean("success")){

                        JSONArray array = new JSONArray(object.getString("result"));
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject scheduleObject = array.getJSONObject(i);
                            ScheduleResidents scheduleResidents = new ScheduleResidents();

                            scheduleResidents.setDay(scheduleObject.getString("day"));
                            scheduleResidents.setWasteType(scheduleObject.getString("type"));
                            scheduleResidents.setStartOfCollection(scheduleObject.getString("startOfCollection"));

                            arrayList.add(scheduleResidents);

                        }
                        scheduleResidentsAdapter = new ScheduleResidentsAdapter(getContext(), arrayList);
                        recyclerView.setAdapter(scheduleResidentsAdapter);
                    }
                } catch(JSONException e){
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

        } else{
            StringRequest request = new StringRequest(Request.Method.POST, constants.SCHEDULE_FOR_RESIDENT, response -> {
                try{
                    JSONObject object = new JSONObject(response);
                    if(object.getBoolean("success")){

                        JSONArray array = new JSONArray(object.getString("result"));
                        for (int i = 0; i < array.length(); i++) {
                            JSONObject scheduleObject = array.getJSONObject(i);
                            ScheduleResidents scheduleResidents = new ScheduleResidents();

                            scheduleResidents.setDay(scheduleObject.getString("day"));
                            scheduleResidents.setWasteType(scheduleObject.getString("type"));
                            scheduleResidents.setStartOfCollection(scheduleObject.getString("startOfCollection"));

                            arrayList.add(scheduleResidents);

                        }
                        scheduleResidentsAdapter = new ScheduleResidentsAdapter(getContext(), arrayList);
                        recyclerView.setAdapter(scheduleResidentsAdapter);
                    }
                } catch(JSONException e){
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

                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> map = new HashMap<String,String>();
                    map.put("street", residentStreet);
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(getContext());
            queue.add(request);
        }
    }




}
