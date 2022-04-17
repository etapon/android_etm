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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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
            finish();
        } else if(role.equals("collector")){
            startActivity(new Intent(MainActivity.this,CollectorsActivity.class));
            finish();
        } else {
            startActivity(new Intent(MainActivity.this,HomeActivity.class));
            finish();
        }
    }


}