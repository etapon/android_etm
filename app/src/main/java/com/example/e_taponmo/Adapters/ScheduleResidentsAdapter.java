package com.example.e_taponmo.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_taponmo.CityActivity;
import com.example.e_taponmo.CityForResidentsActivity;
import com.example.e_taponmo.Models.ScheduleResidents;
import com.example.e_taponmo.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ScheduleResidentsAdapter extends RecyclerView.Adapter<ScheduleResidentsAdapter.ScheduleResidentsHolder>{
    private ArrayList<ScheduleResidents> list;
    private Context context;

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://e-tapon-mo-default-rtdb.firebaseio.com/");
    private DatabaseReference activeStatusDb = database.getReference("activeStatus");
    private Boolean isActive;

    public ScheduleResidentsAdapter(Context context, ArrayList<ScheduleResidents> list){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ScheduleResidentsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_schedule_residents, parent,  false);
        return new ScheduleResidentsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScheduleResidentsHolder holder, int position) {
        ScheduleResidents scheduleResidents = list.get(position);


        holder.txtDayResidents.setText(scheduleResidents.getDay());
        holder.txtTypeResidents.setText(scheduleResidents.getWasteType());
        holder.txtStartOfCollectionResidents.setText(scheduleResidents.getStartOfCollection());

        GradientDrawable activeStatus = (GradientDrawable) holder.scheduleActiveStatusResidents.getBackground();

        if(scheduleResidents.getWasteType().equals("Biodegradable")){
            holder.scheduleImageTypeResidents.setBackground(ContextCompat.getDrawable(context, R.drawable.bio_logo));
        } else if(scheduleResidents.getWasteType().equals("non-Biodegradable")){
            holder.scheduleImageTypeResidents.setBackground(ContextCompat.getDrawable(context, R.drawable.non_bio_logo));
        } else {
            holder.scheduleImageTypeResidents.setBackground(ContextCompat.getDrawable(context, R.drawable.recyclable_logo));
        }

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        Format f = new SimpleDateFormat("EEEE");
        String str = f.format(new Date());

        if(str.equals(scheduleResidents.getDay())){
            activeStatus.setColor(context.getResources().getColor(R.color.color_active));
            holder.txtScheduleActiveStatusResidents.setText("Day of Collection");
        }else {
            activeStatus.setColor(context.getResources().getColor(R.color.color_offline));
            holder.txtScheduleActiveStatusResidents.setText("Offline");
        }

        activeStatusDb.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try{
                    String value = dataSnapshot.getValue(String.class);
                    if(value.equals("active")){
                        isActive = true;
                    } else {
                        isActive = false;
                    };
                } catch(DatabaseException e){
                    Toast.makeText(context, "Can't connect to firebase", Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.out.println(error);
            }
        });

        holder.layoutScheduleResidents.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(scheduleResidents.getDay().equals(str)){
                    if(isActive != null && isActive == true){
                        context.startActivity(new Intent(context, CityForResidentsActivity.class));
                    } else {
                        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                        builder.setMessage("Sorry! There is no active collector right now, please try again later")
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.cancel();
                                    }
                                });
                        androidx.appcompat.app.AlertDialog alertDialog = builder.create();
                        alertDialog.show();
                    }
                } else{
                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(context);
                    builder.setMessage("Please pick a correct schedule!  This schedule is for "+scheduleResidents.day+" only")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.cancel();
                                }
                            });
                    androidx.appcompat.app.AlertDialog alertDialog = builder.create();
                    alertDialog.show();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ScheduleResidentsHolder extends RecyclerView.ViewHolder{
        private TextView txtDayResidents, txtTypeResidents, txtStartOfCollectionResidents, txtScheduleActiveStatusResidents;
        private LinearLayout scheduleImageTypeResidents, layoutScheduleResidents, scheduleActiveStatusResidents;
        public ScheduleResidentsHolder(@NonNull View itemView) {
            super(itemView);

            txtDayResidents = itemView.findViewById(R.id.txtScheduleDayResidents);
            txtTypeResidents = itemView.findViewById(R.id.txtScheduleTypeResidents);
            txtStartOfCollectionResidents = itemView.findViewById(R.id.txtScheduleStartResident);
            txtScheduleActiveStatusResidents = itemView.findViewById(R.id.txtScheduleActiveStatusResidents);

            scheduleImageTypeResidents = itemView.findViewById(R.id.scheduleImageTypeResidents);
            layoutScheduleResidents = itemView.findViewById(R.id.layoutScheduleResidents);
            scheduleActiveStatusResidents = itemView.findViewById(R.id.scheduleActiveStatusResidents);

        }
    }

}
