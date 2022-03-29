package com.example.e_taponmo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PackageManagerCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.mapbox.mapboxsdk.maps.MapView;

import java.util.Random;


public class MainActivity extends AppCompatActivity {
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(getApplication(),MyService.class));
        createNotification();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
                boolean isLoggedIn = userPref.getBoolean("isLoggedIn", false);

                if(isLoggedIn){
                    checkRole();
                } else {
                    isFirstTime();
                }

            }
        },1500);

    }

    public void createNotification(){
        Toast.makeText(this, "dapat may notif e", Toast.LENGTH_SHORT).show();
        String id = "collection_channel";
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel = manager.getNotificationChannel(id);
            if(channel == null){
                channel = new NotificationChannel(id, "Collection Notification", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Collector is Active come to our map and find out the location");
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100,1000,200,340});
                channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                manager.createNotificationChannel(channel);
            }
        }
        Intent notificationIntent = new Intent(this,NotificationActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,notificationIntent,0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,id)
                .setSmallIcon(R.drawable.etm_logo)
                .setContentTitle("Collection Notification")
                .setContentText("A collector is active, come and find out its location")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[]{100,1000,200,340})
                .setAutoCancel(false)//true touch on notificaiton menu dismissed, but swipe to dismiss
                .setTicker("Notification");
        builder.setContentIntent(contentIntent);
        NotificationManagerCompat m = NotificationManagerCompat.from(getApplicationContext());
        //id to generate new notification in list notifications menu
        m.notify(new Random().nextInt(),builder.build());
    }

    private void isFirstTime() {
        SharedPreferences preferences = getApplication().getSharedPreferences("onBoard", Context.MODE_PRIVATE);
        boolean isFirstTime = preferences.getBoolean("isFirstTime", true);

        if(isFirstTime) {

            SharedPreferences.Editor editor = preferences.edit();
            editor.putBoolean("isFirstTime", false);

            startActivity(new Intent(MainActivity.this,OnBoardActivity.class));
            finish();
        }
        else{
            startActivity(new Intent(MainActivity.this,AuthActivity.class));
            finish();
        }
    }

    private void checkRole(){
        SharedPreferences userPref = getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String role = (userPref.getString("role", ""));

        if (role.equals("resident")){
            startActivity(new Intent(MainActivity.this,HomeActivity.class));
        } else if(role.equals("collector")){
            startActivity(new Intent(MainActivity.this,CollectorsActivity.class));
        } else {
            startActivity(new Intent(MainActivity.this,HomeActivity.class));
        }
    }


}