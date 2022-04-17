package com.example.e_taponmo;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.e_taponmo.Fragments.HomeFragment;
import com.example.e_taponmo.Fragments.ProfileFragment;
import com.example.e_taponmo.Fragments.ScheduleFragment;
import com.example.e_taponmo.Fragments.StreetFragment;

public class CollectorsActivity extends AppCompatActivity {
    MeowBottomNavigation bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_collectors);

        bottomNavigation = findViewById(R.id.collectors_bottom_navigation);

        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.ic_baseline_person_24));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.ic_baseline_schedule_24));

        bottomNavigation.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {
                Fragment fragment = null;

                switch(item.getId()){
                    case 1:
                        fragment = new ProfileFragment();
                        break;
//                    case 2:
//                        fragment = new StreetFragment();
//                        break;
                    case 2:
                        fragment = new ScheduleFragment();
                        break;
                }

                if(fragment != null) {
                    LoadFragment(fragment);
                }

            }
        });

        bottomNavigation.show(2, true);

        bottomNavigation.setOnClickMenuListener(new MeowBottomNavigation.ClickListener() {
            @Override
            public void onClickItem(MeowBottomNavigation.Model item) {
//                Toast.makeText(getApplicationContext(), "You clicked "+ item.getId(), Toast.LENGTH_SHORT).show();
            }
        });
        bottomNavigation.setOnReselectListener(new MeowBottomNavigation.ReselectListener() {
            @Override
            public void onReselectItem(MeowBottomNavigation.Model item) {
//                Toast.makeText(getApplicationContext(), "You reselect "+ item.getId(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void LoadFragment(Fragment fragment){
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.collectors_frame_layout, fragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure, you want to exit?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CollectorsActivity.super.onBackPressed();
                        System.exit(1);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.cancel();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

}