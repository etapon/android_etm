package com.example.e_taponmo.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_taponmo.CityActivity;
import com.example.e_taponmo.CityForResidentsActivity;
import com.example.e_taponmo.Models.Street;
import com.example.e_taponmo.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class StreetAdapter extends RecyclerView.Adapter<StreetAdapter.StreetHolder>{
    private ArrayList<Street> list;
    private Context context;

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://e-tapon-mo-default-rtdb.firebaseio.com/");

    private DatabaseReference streetAssigned = database.getReference("streetAssigned");

    public StreetAdapter(Context context, ArrayList<Street> list){
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public StreetHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_street, parent, false);
        return new StreetHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StreetHolder holder, int position) {
        Street street = list.get(position);
        holder.txtStreetName.setText(street.getStreetName());
        holder.streetCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                streetAssigned.setValue(street.getStreetName());
                context.startActivity(new Intent(context, CityActivity.class));
            }
        });

    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class StreetHolder extends RecyclerView.ViewHolder{

        private TextView txtStreetName;
        private LinearLayout streetCard;

        public StreetHolder(@NonNull View itemView) {
            super(itemView);
            txtStreetName = itemView.findViewById(R.id.txtStreetName);
            streetCard = itemView.findViewById(R.id.streetCard);
        }
    }

}
