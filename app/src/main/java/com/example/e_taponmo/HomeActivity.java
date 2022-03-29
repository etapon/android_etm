package com.example.e_taponmo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.example.e_taponmo.Fragments.HomeFragment;
import com.example.e_taponmo.Fragments.ProfileFragment;
import com.example.e_taponmo.Fragments.ScheduleForResidentsFragment;
import com.example.e_taponmo.Fragments.ScheduleFragment;

public class HomeActivity extends AppCompatActivity {
    MeowBottomNavigation bottomNavigation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        bottomNavigation = findViewById(R.id.bottom_navigation);

        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.ic_baseline_person_24));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.ic_baseline_home_24));
        bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.ic_baseline_schedule_24));


        bottomNavigation.setOnShowListener(new MeowBottomNavigation.ShowListener() {
            @Override
            public void onShowItem(MeowBottomNavigation.Model item) {
                Fragment fragment = null;

                switch (item.getId()){
                    case 1:
                        fragment = new ProfileFragment();
                        break;
                    case 2:
                        fragment = new HomeFragment();
                        break;
                    case 3:
                        fragment = new ScheduleForResidentsFragment();
                        break;
                }
                if(fragment != null){
                    loadFragment(fragment);
                }

            }
        });

//        bottomNavigation.setCount(2, "10");
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

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame_layout,fragment)
                .commit();
    }
}