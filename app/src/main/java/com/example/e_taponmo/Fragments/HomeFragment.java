package com.example.e_taponmo.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.e_taponmo.Adapters.AnnouncementAdapter;
import com.example.e_taponmo.HomeActivity;
import com.example.e_taponmo.Models.Announcement;
import com.example.e_taponmo.R;
import com.example.e_taponmo.constants;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {
    private View view;
    private RecyclerView recyclerView;
    private ArrayList<Announcement> arrayList;
    private SwipeRefreshLayout refreshLayout;
    private AnnouncementAdapter announcementAdapter;
    private MaterialToolbar toolbar;
    private SharedPreferences sharedPreferences;
    private Context context;

    public HomeFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_home, container, false);
        context = view.getContext();
        init();
        return view;
    }

    private void init(){

        sharedPreferences = getContext().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        recyclerView = view.findViewById(R.id.recyclerHome);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayout = view.findViewById(R.id.swipeHome);
        toolbar = view.findViewById(R.id.toolBarHome);
        ((HomeActivity)getContext()).setSupportActionBar(toolbar);

        getAnnouncements();

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getAnnouncements();
            }
        });
    }

    private void getAnnouncements(){
        arrayList = new ArrayList<>();
        refreshLayout.setRefreshing(true);

        StringRequest request = new StringRequest(Request.Method.GET, constants.ANNOUNCEMENTS, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if(object.getBoolean("success")){
                    JSONArray array = new JSONArray(object.getString("data"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject announcementObject = array.getJSONObject(i);
                        Announcement announcement = new Announcement();

                        announcement.setProfile(announcementObject.getString("profile"));
                        announcement.setCreator(announcementObject.getString("creator"));
                        announcement.setTitle(announcementObject.getString("title"));
                        if(announcementObject.has("street")) {
                            announcement.setStreet(announcementObject.getString("street"));
                        } else {
                            announcement.setStreet(null);
                        }
                        announcement.setDesc(announcementObject.getString("message"));
                        announcement.setDate(announcementObject.getString("createdAt"));
                        announcement.setSelectedFile(announcementObject.getString("selectedFile"));

                        arrayList.add(announcement);
                    }

                    announcementAdapter = new AnnouncementAdapter(getContext(), arrayList);
                    recyclerView.setAdapter(announcementAdapter);
                } else{
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                    builder.setMessage(""+object.getString("message"))
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    androidx.appcompat.app.AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            refreshLayout.setRefreshing(false);

        }, error -> {
            error.printStackTrace();
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
            builder.setMessage("An error occurred! Please check your internet connection")
                    .setCancelable(false)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.cancel();
                        }
                    });
            androidx.appcompat.app.AlertDialog alertDialog = builder.create();
            alertDialog.show();
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
