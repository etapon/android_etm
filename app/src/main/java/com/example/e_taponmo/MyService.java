package com.example.e_taponmo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyService extends Service {

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://e-tapon-mo-default-rtdb.firebaseio.com/");
    private DatabaseReference activeStatus = database.getReference("activeStatus");
    private DatabaseReference streetAssigned = database.getReference("streetAssigned");
    private DatabaseReference wasteType = database.getReference("wasteType");

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        activeStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                if(value.equals("active")){
                    Toast.makeText(getApplicationContext(), "nag active", Toast.LENGTH_SHORT).show();
                }
//                Log.d(TAG, "Value is: " + value);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                System.out.println("Failed to read value"+error.toException());
//                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
        return START_STICKY;
    }
}
