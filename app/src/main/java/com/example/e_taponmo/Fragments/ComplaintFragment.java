package com.example.e_taponmo.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
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
import com.example.e_taponmo.Adapters.ComplaintAdapter;
import com.example.e_taponmo.CityActivity;
import com.example.e_taponmo.HomeActivity;
import com.example.e_taponmo.Models.Announcement;
import com.example.e_taponmo.Models.Complaint;
import com.example.e_taponmo.R;
import com.example.e_taponmo.constants;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ComplaintFragment extends Fragment {
    private View view;
    private RecyclerView recyclerView;
    private ArrayList<Complaint> arrayList;
    private SwipeRefreshLayout refreshLayout;
    private ComplaintAdapter complaintAdapter;
    private MaterialToolbar toolbar;
    private SharedPreferences sharedPreferences;
    private FloatingActionButton complaintAdd;
    private Dialog dialog;
    private Context context;
    private TextView residentComplaintTitle, residentComplaintDescription;
    private String residentName, residentStreet, residentId;

    public ComplaintFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_complaints, container, false);
        init();
        return view;
    }

    private void init(){
        complaintAdd = view.findViewById(R.id.complaintAdd);
        sharedPreferences = getContext().getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        residentId = sharedPreferences.getString("id", "");
        residentStreet = sharedPreferences.getString("street", "");
        residentName = sharedPreferences.getString("name", "");
        context = getContext();
        dialog = new Dialog(getContext());
        dialog.setContentView(R.layout.layout_add_complaint);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        residentComplaintTitle = dialog.findViewById(R.id.residentComplaintTitle);
        residentComplaintDescription = dialog.findViewById(R.id.residentComplaintDescription);
        Button btnCreate = dialog.findViewById(R.id.btnComplaintCreate);
        Button btnCancel = dialog.findViewById(R.id.btnComplaintCancel);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        recyclerView = view.findViewById(R.id.recyclerAdminComplaint);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        refreshLayout = view.findViewById(R.id.swipeAdminComplaint);
        toolbar = view.findViewById(R.id.toolBarComplaint);
        ((HomeActivity)getContext()).setSupportActionBar(toolbar);

        String role = sharedPreferences.getString("role", "");

        if(role.equals("admin")){
            complaintAdd.setVisibility(view.INVISIBLE);
            getComplaints();
        } else {
            getComplaintsResident();
        }



        complaintAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
            }
        });

        btnCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                StringRequest request = new StringRequest(Request.Method.POST, constants.CREATE_COMPLAINT, response -> {

                    try {
                        JSONObject object = new JSONObject(response);
                        if(object.getBoolean("success")){
                            Toast.makeText(context, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(context, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                        }
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

                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String,String> map = new HashMap<String,String>();

                        String image = (sharedPreferences.getString("image", ""));

                        map.put("residentId", residentId);
                        map.put("residentName", residentName);
                        map.put("residentStreet", residentStreet);
                        map.put("title", residentComplaintTitle.getText().toString());
                        map.put("description", residentComplaintDescription.getText().toString());
                        map.put("residentProfile", image);
                        return map;
                    }
                };

                RequestQueue queue = Volley.newRequestQueue(context);
                queue.add(request);
                dialog.dismiss();

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(role.equals("admin")){
                    getComplaints();
                } else{
                    getComplaintsResident();
                }


            }
        });
    }

    public void createComplaint(Context context){

    }
    public void getComplaintsResident(){
        arrayList = new ArrayList<>();
        refreshLayout.setRefreshing(true);
        String id = sharedPreferences.getString("id", "");
        StringRequest request = new StringRequest(Request.Method.GET, constants.COMPLAINTS_FOR_RESIDENT+id, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if(object.getBoolean("success")){
                    JSONArray array = new JSONArray(object.getString("result"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject complaintObject = array.getJSONObject(i);
                        Complaint complaint = new Complaint();

                        complaint.setComplaintId(complaintObject.getString("_id"));
                        complaint.setTitle(complaintObject.getString("title"));
                        complaint.setDescription(complaintObject.getString("description"));
                        complaint.setResidentName(complaintObject.getString("residentName"));
                        complaint.setResidentStreet(complaintObject.getString("residentStreet"));
                        complaint.setComplaintDate(complaintObject.getString("createdAt"));
                        complaint.setResidentProfile(complaintObject.getString("residentProfile"));

                        arrayList.add(complaint);
                    }
                    complaintAdapter = new ComplaintAdapter(getContext(), arrayList);
                    recyclerView.setAdapter(complaintAdapter);
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
    public void getComplaints(){
        arrayList = new ArrayList<>();
        refreshLayout.setRefreshing(true);

        StringRequest request = new StringRequest(Request.Method.GET, constants.COMPLAINTS_FOR_ADMIN, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if(object.getBoolean("success")){
                    JSONArray array = new JSONArray(object.getString("result"));
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject complaintObject = array.getJSONObject(i);
                        Complaint complaint = new Complaint();

                        complaint.setComplaintId(complaintObject.getString("_id"));
                        complaint.setTitle(complaintObject.getString("title"));
                        complaint.setDescription(complaintObject.getString("description"));
                        complaint.setResidentName(complaintObject.getString("residentName"));
                        complaint.setResidentStreet(complaintObject.getString("residentStreet"));
                        complaint.setComplaintDate(complaintObject.getString("createdAt"));
                        complaint.setResidentProfile(complaintObject.getString("residentProfile"));

                        arrayList.add(complaint);
                    }
                    complaintAdapter = new ComplaintAdapter(getContext(), arrayList);
                    recyclerView.setAdapter(complaintAdapter);
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
