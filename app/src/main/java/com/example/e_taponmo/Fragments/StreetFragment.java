package com.example.e_taponmo.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.example.e_taponmo.Adapters.StreetAdapter;
import com.example.e_taponmo.CollectorsActivity;
import com.example.e_taponmo.Models.Schedule;
import com.example.e_taponmo.Models.Street;
import com.example.e_taponmo.R;
import com.example.e_taponmo.constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StreetFragment extends Fragment {
    private View view;
    private RecyclerView recyclerView;
    private ArrayList<Street> arrayList;
    private SwipeRefreshLayout refreshLayout;
    private StreetAdapter streetAdapter;
    private MaterialToolbar toolbar;
    private SharedPreferences sharedPreferences;
    private String collectorsID, bundledId;

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://e-tapon-mo-default-rtdb.firebaseio.com/");

    private DatabaseReference activeStatus = database.getReference("activeStatus");

    public StreetFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_streets, container, false);
        init();
        return view;
    }

    private void init(){
        sharedPreferences = getContext().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        collectorsID = sharedPreferences.getString("id", "");

        Bundle bundle = this.getArguments();
        bundledId = bundle.getString("id", "");

        recyclerView = view.findViewById(R.id.recyclerStreets);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayout = view.findViewById(R.id.swipeStreets);
        toolbar = view.findViewById(R.id.toolBarSchedule);
        ((CollectorsActivity)getContext()).setSupportActionBar(toolbar);

        getStreets();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getStreets();
            }
        });

    }

    public void getStreets(){
        arrayList = new ArrayList<>();
        refreshLayout.setRefreshing(true);
        Toast.makeText(getContext(), "bundled ID:"+bundledId, Toast.LENGTH_SHORT).show();
        StringRequest request = new StringRequest(Request.Method.GET, constants.SCHEDULE+bundledId, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if(object.getBoolean("success")){
                    JSONObject result = object.getJSONObject("result");

                    JSONArray streets = result.getJSONArray("queue");

                    for(int i2 = 0; i2 < streets.length(); i2++){
                        Street street = new Street();
                        System.out.println(streets.get(i2).toString());
                        street.setStreetName(streets.get(i2).toString());
                        arrayList.add(street);
                    }

                    streetAdapter = new StreetAdapter(getContext(), arrayList);
                    recyclerView.setAdapter(streetAdapter);
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
