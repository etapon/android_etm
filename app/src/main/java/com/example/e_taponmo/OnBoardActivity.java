package com.example.e_taponmo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.e_taponmo.Adapters.ViewPagerAdapter;

public class OnBoardActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private Button btnLeft, btnRight;
    private ViewPagerAdapter adapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;

    private int PHONE_STATE_PERMISSION_CODE = 1;
    private int LOCATION_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_board);
        init();
    }

    private void init(){
        viewPager = findViewById(R.id.view_pager);
        btnLeft = findViewById(R.id.btnLeft);
        btnRight = findViewById(R.id.btnRight);
        dotsLayout = findViewById(R.id.dotsLayout);
        adapter = new  ViewPagerAdapter(this);
        addDots(0);
        viewPager.addOnPageChangeListener((listener));
        viewPager.setAdapter(adapter);

        btnRight.setOnClickListener(v->{
            if (btnRight.getText().toString().equals("Next")){
                viewPager.setCurrentItem(viewPager.getCurrentItem()+1);
            }else{
                if(ContextCompat.checkSelfPermission(OnBoardActivity.this,
                        Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(OnBoardActivity.this, "Location Permission Granted!", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(OnBoardActivity.this,AuthActivity.class));
                    finish();
                } else {
                    requestPhoneStatePermission();

                }


            }
        });

        btnLeft.setOnClickListener(v->{
            viewPager.setCurrentItem(viewPager.getCurrentItem()+2);
        });
    }

    private void addDots(int position){
        dotsLayout.removeAllViews();
        dots = new TextView[3];
        for (int i = 0; i < dots.length; i++){
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(getResources().getColor((R.color.colorLightGrey)));
            dotsLayout.addView(dots[i]);
        }

        if(dots.length>0){
            dots[position].setTextColor(getResources().getColor(R.color.colorGrey));

        }
    }

    private ViewPager.OnPageChangeListener listener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            addDots(position);

            if(position==0){
                btnLeft.setVisibility(View.VISIBLE);
                btnLeft.setEnabled(true);
                btnRight.setText("Next");
            }else if(position == 1){
                btnLeft.setVisibility(View.GONE);
                btnLeft.setEnabled(false);
                btnRight.setText("Next");
            }else{
                btnLeft.setVisibility(View.GONE);
                btnLeft.setEnabled(false);
                btnRight.setText("Finish");
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void requestPhoneStatePermission(){
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("these permissions are needed to prevent the application from crashing when using the map")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(OnBoardActivity.this, new String[] {Manifest.permission.READ_PHONE_STATE}, PHONE_STATE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.READ_PHONE_STATE}, PHONE_STATE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == PHONE_STATE_PERMISSION_CODE){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Permission GRANTED", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(OnBoardActivity.this,AuthActivity.class));
                finish();
            } else {
                Toast.makeText(this, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }

}