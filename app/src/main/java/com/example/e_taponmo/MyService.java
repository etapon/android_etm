package com.example.e_taponmo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
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

import java.util.Random;

public class MyService extends Service {

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://e-tapon-mo-default-rtdb.firebaseio.com/");
    private DatabaseReference activeStatus = database.getReference("activeStatus");
    private DatabaseReference streetAssigned = database.getReference("streetAssigned");
    private DatabaseReference wasteType = database.getReference("wasteType");
    private String assigned;
    private String ifActive;
    private String streetOnPref;
    private String roleOnPref;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        streetOnPref = (userPref.getString("street", ""));
        roleOnPref = (userPref.getString("role", ""));

//        activeStatus.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                String value = dataSnapshot.getValue(String.class);
//                ifActive = value;
//                if(value.equals("active")){
//                    streetAssigned.addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            String value = dataSnapshot.getValue(String.class);
//                            if(ifActive == "active" && value.equals(streetOnPref) && roleOnPref.equals("resident")){
//                                createNotification();
//                            }
//                        }
//                        @Override
//                        public void onCancelled(DatabaseError error) {
//                            System.out.println("Failed to read value"+error.toException());
//                        }
//                    });
//
//                    streetAssigned.addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//                            String value = dataSnapshot.getValue(String.class);
//                            if(ifActive == "active" && value.equals(streetOnPref) && roleOnPref.equals("resident")){
//                                createNotification();
//                            }
//                        }
//                        @Override
//                        public void onCancelled(DatabaseError error) {
//                            System.out.println("Failed to read value"+error.toException());
//                        }
//                    });
//                }
//
//            }
//
//            @Override
//            public void onCancelled(DatabaseError error) {
//                System.out.println("Failed to read value"+error.toException());
//            }
//        });

        activeStatus.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String value = dataSnapshot.getValue(String.class);
                ifActive = value;
                if(value.equals("active")){
//                    Toast.makeText(getApplicationContext(), "active value is: "+ifActive, Toast.LENGTH_SHORT).show();
                    streetAssigned.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String value = dataSnapshot.getValue(String.class);
                            if(ifActive.equals("active") && value.equals(streetOnPref) && roleOnPref.equals("resident")){
                                createNotification();
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError error) {
                            System.out.println("Failed to read value"+error.toException());
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println("Failed to read value"+error.toException());
            }
        });







        return START_STICKY;
    }

    public void createNotification(){
        String id = "collection_channel";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = manager.getNotificationChannel(id);
            if(channel == null){
                channel = new NotificationChannel(id, "Collection is happening on your street!", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Visit the app to find out where's the collector...");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100,1000,200,340});
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                manager.createNotificationChannel(channel);
            }
        }
        Intent notificationIntent = new Intent(this,CityForResidentsActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,id)
                .setSmallIcon(R.drawable.etm_logo)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.etm_logo))
                .setContentTitle("Collection is happening on your street!")
                .setContentText("Visit the app to find out where's the collector...")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{100,1000,200,340})
                .setAutoCancel(false)//true touch on notificaiton menu dismissed, but swipe to dismiss
                .setTicker("Notification");
        builder.setContentIntent(contentIntent);
        NotificationManagerCompat m = NotificationManagerCompat.from(getApplicationContext());
        //id to generate new notification in list notifications menu
        m.notify(new Random().nextInt(),builder.build());
    }
}
