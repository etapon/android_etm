package com.example.e_taponmo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
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

import java.util.List;


public class CityForResidentsActivity extends AppCompatActivity implements PermissionsListener, OnMapReadyCallback {

    private static final int PERMISSION_LOCATION = 1000;

    private MapView mapView;
    private MapboxMap mapboxMap;
    private LocationComponent locationComponent;
    private PermissionsManager permissionsManager;
    private com.getbase.floatingactionbutton.FloatingActionButton floatingActionButton, findCollector, backToHome;

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://e-tapon-mo-default-rtdb.firebaseio.com/");

    private TextView txtWeight, txtPercentage, txtAssignedStreet, txtAssignedWaste;

    private DatabaseReference Weight = database.getReference("Weight");
    private DatabaseReference Percentage = database.getReference("TrashBin");
    private DatabaseReference activeStatus = database.getReference("activeStatus");
    private DatabaseReference streetAssigned = database.getReference("streetAssigned");
    private DatabaseReference wasteType = database.getReference("wasteType");
    private DatabaseReference loc = database.getReference("loc");

    private LinearLayout linearView;
    private MarkerOptions options = new MarkerOptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Mapbox.getInstance(this, getString(R.string.access_token));

        setContentView(R.layout.activity_city_for_residents);

        floatingActionButton = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.showResidentLocation);
        findCollector = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.findCollector);
        backToHome = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.backToHome);
        mapView = (MapView) findViewById(R.id.city_map_residents);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        txtWeight = (TextView) findViewById(R.id.txtWeightResidents);
        txtPercentage = (TextView) findViewById(R.id.txtPercentageResidents);
        txtAssignedStreet = (TextView) findViewById(R.id.collectorAssignedStreetResidents);
        txtAssignedWaste = (TextView) findViewById(R.id.collectorAssignedWasteTypeResidents);
        linearView = (LinearLayout) findViewById(R.id.activeCircleResidents);

        GradientDrawable background = (GradientDrawable) linearView.getBackground();

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(locationComponent.getLastKnownLocation().getLatitude(),locationComponent.getLastKnownLocation().getLongitude())).zoom(16).build();
                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500);
            }
        });

        findCollector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activeStatus.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String value = snapshot.getValue(String.class);
                        if(value.equals("idle")){
                            Toast.makeText(getApplicationContext(), "there is no active collector", Toast.LENGTH_SHORT).show();
                        } else {
                            locateCollector();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w("debug", "Failed to read value.", error.toException());
                    }
                });
            }
        });

        backToHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        Weight.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                txtWeight.setText(value + " kg");
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
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("debug", "Failed to read value.", error.toException());
            }
        });

        wasteType.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                txtAssignedWaste.setText(value);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w("debug", "Failed to read value.", error.toException());
            }
        });

    }

    public void locateCollector(){
        activeStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String value = snapshot.getValue(String.class);
                if(value.equals("idle")){
                    Toast.makeText(getApplicationContext(), "there is no active collector", Toast.LENGTH_SHORT).show();
                } else {



                    loc.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Float latitude = snapshot.child("lat").getValue(Float.class);
                            Float longitude = snapshot.child("long").getValue(Float.class);

                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(new LatLng(latitude,longitude)).zoom(16).build();
                            mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 500);

//                            Toast.makeText(getApplicationContext(), "nag uupdate"+latitude+longitude, Toast.LENGTH_SHORT).show();
                            String temp = options.getTitle();
                            if(temp == null){
                                options.title("Collector's Location");
                                options.position(new LatLng(latitude, longitude));
                                IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
                                Icon icon = iconFactory.fromResource(R.drawable.collector_map_icon);
                                options.icon(icon);
                                mapboxMap.addMarker(options);
                            } else {
                                MarkerOptions newMark = new MarkerOptions();
                                newMark.title("Collectors Position");
                                newMark.position(new LatLng(latitude, longitude));
                                IconFactory iconFactory = IconFactory.getInstance(getApplicationContext());
                                Icon icon = iconFactory.fromResource(R.drawable.collector_map_icon);
                                newMark.icon(icon);
                                mapboxMap.removeAnnotations();
                                mapboxMap.addMarker(newMark);
//                                options.position(new LatLng(latitude, longitude));
                                Toast.makeText(getApplicationContext(), "dapat move", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            Log.w("debug", "Failed to read value.", error.toException());
                        }
                    });





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

    public void enableComponent(@NonNull Style loadedMapStyle){
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
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if ((keyCode == KeyEvent.KEYCODE_BACK))
        {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

}