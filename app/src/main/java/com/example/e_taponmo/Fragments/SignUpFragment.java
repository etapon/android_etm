package com.example.e_taponmo.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SignUpFragment extends Fragment {
    private View view;
    private TextInputLayout layoutEmail, layoutPassword, layoutConfirmPassword;
    private TextInputEditText txtFirstName, txtLastName, txtEmail, txtPassword, txtConfirmPassword;
    private AutoCompleteTextView streetSelector;
    private TextView txtSignIn, txtSelectPhoto;
    private Button btnSignUp;
//    private CircleImageView circleImageView;
    private constants CONSTANT;

    private static final int GALLERY_AND_PROFILE = 1;
    private Bitmap bitmap = null;
    private String encodedImage;

    private String[] streetItems = {"Saint Agustine Street", "Saint Rita Street", "Saint Mary Street", "Sta Fe Street", "Sta Rosa Street", "Saint Francis Street", "Saint Cecille Street", "Saint Ann Street", "Saint Elena Street", "Saint Agnes Street", "Saint Isabel Street", "Saint Claire Street", "Pulo Street", "Kia/ Philtranco Street"};
    private ArrayAdapter<String> adapterItems;

    public SignUpFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.layout_sign_up, container, false);
        init();
        return view;
    }

    public void init(){
        layoutEmail = view.findViewById(R.id.txtLayoutEmailSignUp);
        layoutPassword = view.findViewById(R.id.txtLayoutPasswordSignUp);
        layoutConfirmPassword = view.findViewById(R.id.txtLayoutPasswordSignUp);
        txtFirstName = view.findViewById(R.id.txtFirstName);
        txtLastName = view.findViewById(R.id.txtLastName);
        txtEmail = view.findViewById(R.id.txtEmailSignUp);
        txtPassword = view.findViewById(R.id.txtPassWordSignUp);
        txtConfirmPassword = view.findViewById(R.id.txtConfirmPassWordSignUp);
        txtSignIn = view.findViewById(R.id.txtSignIn);
        txtSelectPhoto = view.findViewById(R.id.txtSelectPhoto);
        btnSignUp = view.findViewById(R.id.btnSignUp);
//        circleImageView = view.findViewById(R.id.imgUserProfile);

        streetSelector = view.findViewById(R.id.streetSelector);
        adapterItems = new ArrayAdapter<String>(getContext(), R.layout.street_list, streetItems);
        streetSelector.setAdapter(adapterItems);
        streetSelector.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String item = adapterView.getItemAtPosition(i).toString();
            }
        });


        txtSignIn.setOnClickListener(v->{
            getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameAuthContainer, new SignInFragment()).commit();
        });

        btnSignUp.setOnClickListener(v->{
            if(validate()){
                signup();
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
                if(txtPassword.getText().toString().isEmpty()){
                    layoutPassword.setErrorEnabled(false);
                    layoutConfirmPassword.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        txtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(txtPassword.getText().toString().isEmpty()){
                    layoutPassword.setErrorEnabled(false);
                    layoutConfirmPassword.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

//        txtSelectPhoto.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent i = new Intent(Intent.ACTION_PICK);
//                i.setType("image/*");
//                startActivityForResult(i, GALLERY_AND_PROFILE);
//            }
//        });
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if(requestCode == GALLERY_AND_PROFILE && resultCode == MainActivity.RESULT_OK){
//            Uri imgUri = data.getData();
//
//
//            try {
//                final InputStream imageStream;
//                imageStream = getContext().getContentResolver().openInputStream(imgUri);
//                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
//                encodedImage = encodeImage(selectedImage);
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            circleImageView.setImageURI(imgUri);
//
//            try {
//                bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(),imgUri);
//            } catch(IOException e){
//
//            }
//        }
//    }
//    private String encodeImage(Bitmap bm)
//    {
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        bm.compress(Bitmap.CompressFormat.JPEG,100,baos);
//        byte[] b = baos.toByteArray();
//        String encImage = Base64.encodeToString(b, Base64.URL_SAFE);
//
//        return encImage;
//    }

    private boolean validate(){
//        if(encodedImage == null){
//            Toast.makeText(getContext(),"Please add your photo avatar", Toast.LENGTH_SHORT).show();
//            return false;
//        }
        if(txtEmail.getText().toString().isEmpty()) {
            layoutEmail.setErrorEnabled(true);
            layoutEmail.setError("Email is required");
            return false;
        }
        if(txtPassword.getText().toString().length()<8){
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError("Required at least 8 characters");
            return false;
        }
        return true;
    }

    private void signup(){

        StringRequest request = new StringRequest(Request.Method.POST, CONSTANT.SIGNUP, response -> {
            try {
                JSONObject object = new JSONObject(response);
                if(object.getBoolean("success")){
                    Toast.makeText(getContext(), "Success", Toast.LENGTH_LONG).show();
                    JSONObject user = object.getJSONObject("result");

                    Toast.makeText(getContext(), ""+object.getString("message"), Toast.LENGTH_LONG).show();

                    getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.frameAuthContainer, new SignInFragment()).commit();
                } else{
                    Toast.makeText(getContext(), ""+object.getString("message"), Toast.LENGTH_LONG).show();
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }, error -> {
           error.printStackTrace();
        }){

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<String,String>();
                map.put("firstName",txtFirstName.getText().toString());
                map.put("lastName",txtLastName.getText().toString());
                map.put("email",txtEmail.getText().toString());
                map.put("street",streetSelector.getText().toString());
                map.put("password",txtPassword.getText().toString());
                map.put("confirmPassword",txtConfirmPassword.getText().toString());
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getContext());
        queue.add(request);
    }

}
