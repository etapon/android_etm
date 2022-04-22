package com.example.e_taponmo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class CityActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback, GPSListener, Serializable {

    private static final int PERMISSION_LOCATION = 1000;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;
    private PermissionsManager permissionsManager;
    private com.getbase.floatingactionbutton.FloatingActionButton startCollecting, finishCollecting, backToSchedules, collectorCollect;
    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://e-tapon-mo-default-rtdb.firebaseio.com/");
    private MediaPlayer player;

    private TextView txtWeight, txtPercentage, txtAssignedStreet, txtAssignedWaste;
    private DatabaseReference Weight = database.getReference("Weight");
    private DatabaseReference Percentage = database.getReference("TrashBin");
    private DatabaseReference activeStatus = database.getReference("activeStatus");
    private DatabaseReference streetAssigned = database.getReference("streetAssigned");
    private DatabaseReference wasteType = database.getReference("wasteType");
    private DatabaseReference loc = database.getReference("loc");
    private DatabaseReference unloading = database.getReference("unloading");

    private LinearLayout linearView;
    private Dialog dialog, dialog_percentage_warning, dialog_weight_warning, dialog_weight_follow_up;

    //for collection
    private String finalizedWeight, finalizedStreet, finalizedType, finalizedDate;
    private String checkActive;
    private ArrayList<String> streetList;
    private Queue streetQueue = new LinkedList<>();

    private int weightValue;
    private String streetValue;

    private int recentWeight;
    private int newWeight;

    private Map<String, String> collectionMap = new HashMap<String, String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        LocationManager lm = (LocationManager)CityActivity.this.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch(Exception ex) {}

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch(Exception ex) {}

        if(!gps_enabled && !network_enabled) {
            // notify user
            new AlertDialog.Builder(CityActivity.this)
                    .setMessage("Your device location is turned off, would you like to turn it on?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            CityActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .show();
        }

        Mapbox.getInstance(this, getString(R.string.access_token));

        streetList = getIntent().getStringArrayListExtra("streetQueue");

        for(int i = 0; i < streetList.size(); i++){
            streetQueue.add(streetList.get(i));
        }

        setStreet();

        setContentView(R.layout.activity_city);

        startCollecting = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.collectorStartCollecting);
        collectorCollect = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.collectorCollect);
        finishCollecting = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.collectorFinishCollecting);
        backToSchedules = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.backToSchedules);

        linearView = (LinearLayout) findViewById(R.id.activeCircle);
        txtAssignedStreet = (TextView) findViewById(R.id.collectorAssignedStreet);
        txtAssignedWaste = (TextView) findViewById(R.id.collectorAssignedWasteType);

        dialog = new Dialog(CityActivity.this);
        dialog.setContentView(R.layout.collection_report_layout);
        dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.collection_report_background));
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        dialog_percentage_warning = new Dialog(CityActivity.this);
        dialog_percentage_warning.setContentView(R.layout.collection_percentage_alert_layout);
        dialog_percentage_warning.getWindow().setBackgroundDrawable(getDrawable(R.drawable.collection_report_background));
        dialog_percentage_warning.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_percentage_warning.setCancelable(false);

        dialog_weight_warning = new Dialog(CityActivity.this);
        dialog_weight_warning.setContentView(R.layout.collection_weight_alert_layout);
        dialog_weight_warning.getWindow().setBackgroundDrawable(getDrawable(R.drawable.collection_report_background));
        dialog_weight_warning.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_weight_warning.setCancelable(false);

        dialog_weight_follow_up = new Dialog(CityActivity.this);
        dialog_weight_follow_up.setContentView(R.layout.collection_weight_follow_up);
        dialog_weight_follow_up.getWindow().setBackgroundDrawable(getDrawable(R.drawable.collection_report_background));
        dialog_weight_follow_up.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog_weight_follow_up.setCancelable(false);

        Button WeightDangerButton = dialog_weight_warning.findViewById(R.id.btnWeightDanger);
        Button PercentageDangerButton = dialog_percentage_warning.findViewById(R.id.btnPercentageDanger);
        Button btnOkay = dialog.findViewById(R.id.btn_okay);
        Button btnCancel = dialog.findViewById(R.id.btn_cancel);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        GradientDrawable background = (GradientDrawable) linearView.getBackground();

        java.util.Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = formatter.format(date);
        finalizedDate = currentDate;

        SharedPreferences userPref = this.getSharedPreferences("user", Context.MODE_PRIVATE);
        String role = (userPref.getString("role", ""));


        mapView = (MapView) findViewById(R.id.city_map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        WeightDangerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(collectionMap.size() >= 1){
                    newWeight = weightValue - recentWeight;
                    String s=String.valueOf(newWeight);
                    collectionMap.put(streetValue, s);
                    recentWeight = recentWeight + newWeight;

                    System.out.println("recent is: "+recentWeight);
                    System.out.println("new is: "+newWeight);
                    System.out.println(collectionMap);
                    mapCollect();
                } else {
                    saveData();
                }

                dialog_weight_warning.hide();
                dialog_weight_follow_up.show();
            }
        });

        PercentageDangerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(collectionMap.size() >= 1){
                    newWeight = weightValue - recentWeight;
                    String s=String.valueOf(newWeight);
                    collectionMap.put(streetValue, s);
                    recentWeight = recentWeight + newWeight;

                    System.out.println("recent is: "+recentWeight);
                    System.out.println("new is: "+newWeight);
                    System.out.println(collectionMap);
                    mapCollect();
                } else {
                    saveData();
                }

                dialog_percentage_warning.hide();
                dialog_weight_follow_up.show();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        btnOkay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(collectionMap.size() >= 1){
                    if(recentWeight < weightValue) {
                        newWeight = weightValue - recentWeight;
                        String s = String.valueOf(newWeight);
                        collectionMap.put(streetValue, s);
                        mapCollect();
                    }
                } else {
                    saveData();
                }
                dialog.dismiss();
                if(weightValue >= 1 ){
                    dialog_weight_follow_up.show();
                }
            }
        });


        startCollecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationManager lm = (LocationManager)CityActivity.this.getSystemService(Context.LOCATION_SERVICE);
                boolean gps_enabled = false;
                boolean network_enabled = false;

                try {
                    gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
                } catch(Exception ex) {}

                try {
                    network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                } catch(Exception ex) {}

                if(!gps_enabled && !network_enabled) {
                    // notify user
                    new AlertDialog.Builder(CityActivity.this)
                            .setMessage("Your device location is turned off, would you like to turn it on?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                    CityActivity.this.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                            .setNegativeButton("Cancel",null)
                            .show();
                } else {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION);
                    } else {
                        showLocation();
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(locationComponent.getLastKnownLocation().getLatitude(),locationComponent.getLastKnownLocation().getLongitude())).zoom(18).build();
                        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500);

                        activeStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                String value = snapshot.getValue(String.class);
                                if(value.equals("idle")){

                                } else {

                                    DatabaseReference Lat = database.getReference("loc").child("lat");
                                    Lat.setValue(locationComponent.getLastKnownLocation().getLatitude());

                                    DatabaseReference Long = database.getReference("loc").child("long");
                                    Long.setValue(locationComponent.getLastKnownLocation().getLongitude());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.w("debug", "Failed to read value.", error.toException());
                            }
                        });

                    }
                }



            }
        });

        collectorCollect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkActive.equals("active")){
                    dialog.show();

                } else{
                    Toast.makeText(getApplicationContext(), "Collecting isn't started yet", Toast.LENGTH_SHORT).show();
                }
            }
        });

        finishCollecting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(weightValue >= 1){
                    if(recentWeight == 0){
                        recentWeight = weightValue;
                        newWeight = weightValue;
                        String s=String.valueOf(newWeight);
                        collectionMap.put(streetValue, s);
                    } else if(recentWeight == weightValue){
                        Toast.makeText(getApplicationContext(), "you skipped a street", Toast.LENGTH_SHORT).show();
                    } else if(recentWeight > weightValue){
                        String s=String.valueOf(weightValue);
                        collectionMap.put(streetValue, s);
                        recentWeight = weightValue;
                    }else {
                        newWeight = weightValue - recentWeight;
                        String s=String.valueOf(newWeight);
                        collectionMap.put(streetValue, s);
                        recentWeight = recentWeight + newWeight;
                    }
                }
                Toast.makeText(getApplicationContext(), "Proceeding to next street!", Toast.LENGTH_SHORT).show();
                streetQueue.poll();
                setStreet();
                System.out.println("recent is: "+recentWeight);
                System.out.println("new is: "+newWeight);
                System.out.println(collectionMap);
            }
        });

        backToSchedules.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(CityActivity.this)
                        .setMessage("Are you sure? Map Activity will reset")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                                activeStatus.setValue("idle");
                                finish();
                            }
                        })
                        .setNegativeButton("Cancel",null)
                        .show();
            }
        });





        Percentage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                txtPercentage = findViewById(R.id.txtPercentageValue);
                txtPercentage.setText(value + "%");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("debug", "Failed to read value.", error.toException());
            }
        });


        activeStatus.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                checkActive = value;
                if(value.equals("idle")){
                    background.setColor(getResources().getColor(R.color.color_offline));
                } else {
                    background.setColor(getResources().getColor(R.color.color_active));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("debug", "Failed to read value.", error.toException());
            }
        });

        activeStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                checkActive = value;
                if(value.equals("idle")){
                    background.setColor(getResources().getColor(R.color.color_offline));
                } else {
                    background.setColor(getResources().getColor(R.color.color_active));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("debug", "Failed to read value.", error.toException());
            }
        });


        streetAssigned.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                txtAssignedStreet.setText(value);
                finalizedStreet = value;
                streetValue = value;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("debug", "Failed to read value.", error.toException());
            }
        });

        streetAssigned.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                txtAssignedStreet.setText(value);
                finalizedStreet = value;
                streetValue = value;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("debug", "Failed to read value.", error.toException());
            }
        });

        wasteType.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                txtAssignedWaste.setText(value);
                finalizedType = value;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("debug", "Failed to read value.", error.toException());
            }
        });

        Percentage.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                int converted = Integer.parseInt(value);
                if(converted >= 100){
                    dialog_percentage_warning.show();
                    unloading.setValue("yes");
                    if(player == null){
                        player = MediaPlayer.create(CityActivity.this, R.raw.warning_sound);
                    }
                    player.start();
                } else{
                    dialog_percentage_warning.hide();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("debug", "Failed to read value.", error.toException());
            }
        });


        Weight.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                float converted = Float.parseFloat(value);
                weightValue = Math.round(converted);
                txtWeight = findViewById(R.id.txtWeightValue);
                txtWeight.setText(value + " kg");
                finalizedWeight = value;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("debug", "Failed to read value.", error.toException());
            }
        });



        Weight.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                float converted = Float.parseFloat(value);

                weightValue = Math.round(converted);
                txtWeight = findViewById(R.id.txtWeightValue);
                txtWeight.setText(value + " kg");
                finalizedWeight = value;

                if(converted >= 180){
                    dialog_weight_warning.show();
                    unloading.setValue("yes");
                    if(player == null){
                        player = MediaPlayer.create(CityActivity.this, R.raw.warning_sound);
                    }
                    player.start();
                } else if(converted < 1 ){
                    dialog_weight_warning.hide();
                    dialog_weight_follow_up.hide();
                    unloading.setValue("no");
                }
                else{
                    dialog_weight_warning.hide();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("debug", "Failed to read value.", error.toException());
            }
        });

    }

    private void setStreet(){
        if(streetQueue.size() >= 1){
            streetAssigned.setValue(streetQueue.peek().toString());
        } else {
            Toast.makeText(this, "collection map size is: "+collectionMap.size(), Toast.LENGTH_SHORT).show();

            if(collectionMap.size() >= 1){
                mapCollect();
            }

            Toast.makeText(this, "No more Street to Collect!", Toast.LENGTH_SHORT).show();
            activeStatus.setValue("idle");
            finish();
        }

    }

    private void mapCollect(){
        for (String street : collectionMap.keySet())
        {
            String weight = collectionMap.get(street);
            System.out.println("Key = " + street + ", Value = " + weight);
            int converted = Integer.parseInt(weight);

            if (converted >= 1) {
                StringRequest request = new StringRequest(Request.Method.POST, constants.COLLECTION, response -> {
                    try {
                        JSONObject object = new JSONObject(response);
                        if(object.getBoolean("success")){
                            Toast.makeText(this, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                        } else{
                            Toast.makeText(this, "error: "+object.getString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    dialog.dismiss();
                }, error -> {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }){
                    @Nullable
                    @Override
                    protected Map<String, String> getParams() throws AuthFailureError {
                        HashMap<String,String> map = new HashMap<String,String>();
                        map.put("street", street);
                        map.put("type", finalizedType);
                        map.put("weight", weight);
                        map.put("date", finalizedDate);
                        return map;
                    }
                };
                RequestQueue queue = Volley.newRequestQueue(this);
                queue.add(request);
            }
        }
        collectionMap.clear();
    }

    private void saveData(){

        if(weightValue >= 1){
            StringRequest request = new StringRequest(Request.Method.POST, constants.COLLECTION, response -> {
                try {
                    JSONObject object = new JSONObject(response);
                    if(object.getBoolean("success")){
                        Toast.makeText(this, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                    } else{
                        Toast.makeText(this, "error: "+object.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
                dialog.dismiss();
            }, error -> {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG).show();
                error.printStackTrace();
            }){
                @Nullable
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    HashMap<String,String> map = new HashMap<String,String>();
                    map.put("street", finalizedStreet);
                    map.put("type", finalizedType);
                    map.put("weight", finalizedWeight);
                    map.put("date", finalizedDate);
                    return map;
                }
            };
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(request);
        } else {
            Toast.makeText(this, "Trash bin is considered empty!", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLocation(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else{
            Toast.makeText(this, "please enable your GPS", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        activeStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                if(value.equals("idle")){

                } else {

                    DatabaseReference Lat = database.getReference("loc").child("lat");
                    Lat.setValue(location.getLatitude());

                    DatabaseReference Long = database.getReference("loc").child("long");
                    Long.setValue(location.getLongitude());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("debug", "Failed to read value.", error.toException());
            }
        });
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {
                enableComponent(style);
            }
        });
    }

    public void enableComponent(@NonNull Style loadedMapStyle) {
        try {
            if (PermissionsManager.areLocationPermissionsGranted(this)) {
                locationComponent = mapboxMap.getLocationComponent();
                locationComponent.activateLocationComponent(
                        LocationComponentActivationOptions.builder(this, loadedMapStyle).build()
                );

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                locationComponent.setLocationComponentEnabled(true);
                locationComponent.setCameraMode(CameraMode.TRACKING);
                locationComponent.setRenderMode(RenderMode.COMPASS);


            } else{
                permissionsManager = new PermissionsManager(this);
                permissionsManager.requestLocationPermissions(this);
            }
        }catch (Exception e){
            Log.e("err_load_map",e.getMessage());
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    @Override
    public void onPermissionResult(boolean granted) {
        if(granted){
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableComponent(style);
                }
            });
        }else{
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(CityActivity.this)
                .setMessage("Are you sure? Map Activity will reset")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        activeStatus.setValue("idle");
                        finish();
                    }
                })
                .setNegativeButton("Cancel",null)
                .show();
    }
}