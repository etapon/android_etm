package com.example.e_taponmo.Adapters;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.text.Layout;
import android.text.format.DateUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.e_taponmo.Models.Complaint;
import com.example.e_taponmo.R;
import com.example.e_taponmo.constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ComplaintHolder> {
    private Context context;
    private ArrayList<Complaint> list;
    private String base64Image, getBase64ImageProfile, complaintId;
    private Dialog dialog, dialogDisplay;
    private TextView residentComplaintTitleEdit, residentComplaintDescriptionEdit, complaintTitleDisplay, complaintNameDisplay, complaintDescriptionDisplay, complaintResidentEmailDisplay;
    private String role;
    public ComplaintAdapter(Context context, ArrayList<Complaint> list){
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ComplaintHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_complaint,parent, false);
        return new ComplaintHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComplaintHolder holder, int position) {
        Complaint complaint = list.get(position);

        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        role = sharedPreferences.getString("role", "");
        if(role.equals("admin")){
            holder.complaintEdit.setVisibility(View.INVISIBLE);
        }
        if(complaint.getSeen().equals(false)){
            holder.complaintSeen2.setTextColor(Color.parseColor("#A7A7A7"));
            holder.complaintSeen.setVisibility(View.INVISIBLE);
            holder.complaintSeen2.setText("not seen");
        }

        String TimeAgo = getTimeAgo(complaint.getComplaintDate().toString());

        dialog = new Dialog(context);
        dialog.setContentView(R.layout.layout_edit_complaint);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);
        residentComplaintTitleEdit = dialog.findViewById(R.id.residentComplaintTitleEdit);
        residentComplaintDescriptionEdit = dialog.findViewById(R.id.residentComplaintDescriptionEdit);
        Button btnEdit = dialog.findViewById(R.id.btnComplaintEdit);
        Button btnCancel = dialog.findViewById(R.id.btnComplaintCancelEdit);
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        dialogDisplay = new Dialog(context);
        dialogDisplay.setContentView(R.layout.layout_complaint_details);
        dialogDisplay.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        dialogDisplay.setCancelable(false);
        complaintTitleDisplay = dialogDisplay.findViewById(R.id.complaintTitleDisplay);
        complaintNameDisplay = dialogDisplay.findViewById(R.id.complaintNameDisplay);
        complaintDescriptionDisplay = dialogDisplay.findViewById(R.id.complaintDescriptionDisplay);
        complaintResidentEmailDisplay = dialogDisplay.findViewById(R.id.complaintEmailDisplay);
        Button done = dialogDisplay.findViewById(R.id.btnComplaintDone);
        dialogDisplay.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        base64Image = complaint.getResidentProfile();

        String cleanImage = base64Image.replace("data:image/png;base64,", "").replace("data:image/jpeg;base64,","");
        byte[] decodedString = Base64.decode(cleanImage, 0);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        holder.imgComplaintProfile.setImageBitmap(decodedByte);

        holder.complaintName.setText(complaint.getResidentName());
        holder.complaintDate.setText(TimeAgo);
        holder.complaintTitle.setText(complaint.getTitle());

        holder.complaintCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                complaintTitleDisplay.setText(complaint.getTitle());
                complaintResidentEmailDisplay.setText(complaint.getResidentEmail());
                complaintNameDisplay.setText(complaint.getResidentName());
                complaintDescriptionDisplay.setText(complaint.getDescription());
                complaintId = complaint.getComplaintId();

                dialogDisplay.show();

                if(complaint.getSeen().equals(false) && role.equals("admin")){
                    seenComplaint(complaintId);
                }
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialogDisplay.dismiss();
            }
        });

        holder.complaintDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(view.getContext());
                builder.setMessage("Are you sure? permanently deleting your complaint!")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                deleteComplaint(complaint.getComplaintId());
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.cancel();
                            }
                        });
                androidx.appcompat.app.AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        holder.complaintEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.show();
                residentComplaintTitleEdit.setText(complaint.getTitle());
                residentComplaintDescriptionEdit.setText(complaint.getDescription());
                complaintId = complaint.getComplaintId();
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editComplaint(complaintId);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ComplaintHolder extends RecyclerView.ViewHolder{
        private TextView complaintName, complaintDate, complaintTitle, complaintSeen2;
        private ImageView complaintEdit, complaintDelete, complaintSeen;
        private CircleImageView imgComplaintProfile;
        private CardView complaintCard;

        public ComplaintHolder(@NonNull View itemView) {
            super(itemView);
            complaintName = itemView.findViewById(R.id.complaintName);
            complaintDate = itemView.findViewById(R.id.complaintDate);
            complaintTitle = itemView.findViewById(R.id.complaintTitle);
            complaintEdit = itemView.findViewById(R.id.complaintEdit);
            complaintDelete = itemView.findViewById(R.id.complaintDelete);
            imgComplaintProfile = itemView.findViewById(R.id.imgComplaintProfile);
            complaintCard = itemView.findViewById(R.id.complaintCard);
            complaintSeen = itemView.findViewById(R.id.complaintSeen);
            complaintSeen2 = itemView.findViewById(R.id.complaintSeen2);

        }
    }

    public String getTimeAgo(String providedDate){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC+8"));
        try {
            long time = sdf.parse(providedDate).getTime();
            long now = System.currentTimeMillis();
            CharSequence ago =
                    DateUtils.getRelativeTimeSpanString(time, now, DateUtils.MINUTE_IN_MILLIS);
            return ago+"";
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void deleteComplaint(String complaintId){
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);
        StringRequest request = new StringRequest(Request.Method.DELETE, constants.DELETE_COMPLAINT+complaintId, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if(object.getBoolean("success")){
                    Toast.makeText(context, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            error.printStackTrace();
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token", "");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization", "Bearer "+token);
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
    }

    public void editComplaint(String complaintId){
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);

        StringRequest request = new StringRequest(Request.Method.PATCH, constants.UPDATE_COMPLAINT+complaintId, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if(object.getBoolean("success")){
                    Toast.makeText(context, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            error.printStackTrace();
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token", "");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization", "Bearer "+token);
                return map;
            }

            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String,String> map = new HashMap<String,String>();
                map.put("title", residentComplaintTitleEdit.getText().toString());
                map.put("description", residentComplaintDescriptionEdit.getText().toString());
                return map;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
        dialog.dismiss();
    }

    public void seenComplaint(String complaintId){
        SharedPreferences sharedPreferences = context.getApplicationContext().getSharedPreferences("user", Context.MODE_PRIVATE);

        StringRequest request = new StringRequest(Request.Method.GET, constants.SEEN_COMPLAINT+complaintId, response -> {

            try {
                JSONObject object = new JSONObject(response);
                if(object.getBoolean("success")){
                    Toast.makeText(context, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, ""+object.getString("message"), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }, error -> {
            error.printStackTrace();
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String token = sharedPreferences.getString("token", "");
                HashMap<String,String> map = new HashMap<>();
                map.put("Authorization", "Bearer "+token);
                return map;
            }

        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(request);
        dialog.dismiss();
    }

}
