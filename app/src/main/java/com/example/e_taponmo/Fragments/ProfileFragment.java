package com.example.e_taponmo.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.e_taponmo.AuthActivity;
import com.example.e_taponmo.HomeActivity;
import com.example.e_taponmo.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment {
    private View view;
    private TextView txtUserName, txtUserRole, txtUserStreet, txtUserEmail;
    private ImageView imgUserImage;
    private Button btnUserLogout;
    private ProgressDialog dialog;
    private SharedPreferences.Editor editor;
    private CircleImageView roleIcon;

    public ProfileFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_profile_info, container, false);
        init();
        return view;
    }

    public void init(){
        txtUserName = view.findViewById(R.id.txtUserName);
        txtUserStreet = view.findViewById(R.id.txtUserStreet);
        txtUserRole = view.findViewById(R.id.txtUserRole);
        txtUserEmail = view.findViewById(R.id.txtUserEmail);
        btnUserLogout = view.findViewById(R.id.btnUserLogout);
        imgUserImage = view.findViewById(R.id.imgUserImage);
        roleIcon = view.findViewById(R.id.roleIcon);
        Drawable residentRole = getResources().getDrawable(R.drawable.profile_resident);
        Drawable adminRole = getResources().getDrawable(R.drawable.profile_admin);
        Drawable collectorRole = getResources().getDrawable(R.drawable.profile_collector);

        dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);


        SharedPreferences userPref = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        editor = userPref.edit();
        String email = (userPref.getString("email", ""));
        String name = (userPref.getString("name", ""));
        String role = (userPref.getString("role", ""));
        String street = (userPref.getString("street", ""));
        String image = (userPref.getString("image", ""));

        String cleanImage = image.replace("data:image/png;base64,", "").replace("data:image/jpeg;base64,","");
        byte[] decodedString = Base64.decode(cleanImage, 0);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);


        imgUserImage.setImageBitmap(decodedByte);
        txtUserName.setText(name);
        txtUserStreet.setText(street);
        txtUserRole.setText(role);
        txtUserEmail.setText(email);

        if(role.equals("resident")){
            roleIcon.setImageDrawable(residentRole);
        } else if(role.equals("collector")){
            roleIcon.setImageDrawable(collectorRole);
        } else {
            roleIcon.setImageDrawable(adminRole);
        }


        btnUserLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.setMessage("logging out...");
                dialog.show();
                editor.clear();
                editor.commit();
                getActivity().finish();
                startActivity(new Intent(getContext(), AuthActivity.class));
                dialog.dismiss();
            }
        });

    }
}
