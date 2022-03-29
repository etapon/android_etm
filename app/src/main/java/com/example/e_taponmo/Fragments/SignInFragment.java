package com.example.e_taponmo.Fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.e_taponmo.AuthActivity;
import com.example.e_taponmo.CollectorsActivity;
import com.example.e_taponmo.HomeActivity;
import com.example.e_taponmo.MainActivity;
import com.example.e_taponmo.R;
import com.example.e_taponmo.constants;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignInFragment extends Fragment {
    private View view;
    private TextInputLayout layoutEmail,layoutPassword;
    private TextInputEditText txtEmail,txtPassword;
    private TextView txtSignUp;
    private Button btnSignIn;
    private constants CONSTANT;
    private ProgressDialog dialog;

    public SignInFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_sign_in, container, false);
        init();
        return view;
    }

    public void init(){
        layoutEmail = view.findViewById(R.id.txtLayoutEmailSignIn);
        layoutPassword = view.findViewById(R.id.txtLayoutPasswordSignIn);
        txtEmail = view.findViewById(R.id.txtEmailSignIn);
        txtPassword = view.findViewById((R.id.txtPassWordSignIn));
        txtSignUp = view.findViewById(R.id.txtSignUp);
        btnSignIn = view.findViewById(R.id.btnSignIn);
        dialog = new ProgressDialog(getContext());
        dialog.setCancelable(false);

        txtSignUp.setOnClickListener(v->{
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameAuthContainer, new SignUpFragment()).commit();
        });

        btnSignIn.setOnClickListener(v->{
            if(validate()){
                signin();
            }
        });

        txtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(txtEmail.getText().toString().isEmpty()){
                    layoutEmail.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        txtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(txtPassword.getText().toString().length()>7){
                    layoutPassword.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private boolean validate(){
        if(txtEmail.getText().toString().isEmpty()) {
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Email is required");
            return false;
        }
        if(txtPassword.getText().toString().length()<8){
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError("Required atleast 8 characters");
            return false;
        }
        return true;
    }

    private void signin(){
        dialog.setMessage("logging in...");
        dialog.show();
        StringRequest request = new StringRequest(Request.Method.POST, CONSTANT.SIGNIN, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if(object.getBoolean("success")){
                    JSONObject user = object.getJSONObject("result");
                    SharedPreferences userPref = getActivity().getApplicationContext().getSharedPreferences("user", getContext().MODE_PRIVATE);
                    SharedPreferences.Editor editor = userPref.edit();
                    editor.putString("token", object.getString("token"));
                    editor.putString("id", user.getString("_id"));
                    editor.putString("name", user.getString("name"));
                    editor.putString("street", user.getString("street"));
                    editor.putString("email", user.getString("email"));
                    editor.putString("role", user.getString("role"));
                    editor.putString("image", user.getString("image"));
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();
                    Toast.makeText(getContext(), "Log In Successful", Toast.LENGTH_LONG).show();
                    checkRole();
                    ((AuthActivity)getContext()).finish();
                } else{
                    Toast.makeText(getContext(), "error: "+object.getString("message"), Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                Toast.makeText(getContext(), "Please check your credentials", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            dialog.dismiss();
        }, error -> {
            Toast.makeText(getContext(), "Please check your credentials", Toast.LENGTH_LONG).show();
            error.printStackTrace();
            dialog.dismiss();
        }){
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<String,String>();
                map.put("email", txtEmail.getText().toString());
                map.put("password", txtPassword.getText().toString());
                return map;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }

    public void checkRole(){
        SharedPreferences userPref = getContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        String role = (userPref.getString("role", ""));

        if (role.equals("resident")){
            startActivity(new Intent(getContext(),HomeActivity.class));
        } else if(role.equals("collector")){
            startActivity(new Intent(getContext(), CollectorsActivity.class));
        } else {
            startActivity(new Intent(getContext(),HomeActivity.class));
        }
    }
}
