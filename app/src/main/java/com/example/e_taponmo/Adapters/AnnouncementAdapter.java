package com.example.e_taponmo.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.text.Layout;
import android.text.format.DateUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.e_taponmo.Models.Announcement;
import com.example.e_taponmo.R;
import com.example.e_taponmo.constants;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.AnnouncementHolder> {

    private Context context;
    private ArrayList<Announcement> list;
    private String base64Image, getBase64ImageProfile;

    public AnnouncementAdapter(Context context, ArrayList<Announcement> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public AnnouncementHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_announcement,parent, false);
        return new AnnouncementHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnnouncementHolder holder, int position) {
        Announcement announcement = list.get(position);

        base64Image = announcement.getSelectedFile();
        getBase64ImageProfile = announcement.getProfile();

        String cleanImage = base64Image.replace("data:image/png;base64,", "").replace("data:image/jpeg;base64,","");
        byte[] decodedString = Base64.decode(cleanImage, 0);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        String cleanImageProfile = getBase64ImageProfile.replace("data:image/png;base64,", "").replace("data:image/jpeg;base64,","");
        byte[] decodedStringProfile = Base64.decode(cleanImageProfile, 0);
        Bitmap decodedByteProfile = BitmapFactory.decodeByteArray(decodedStringProfile, 0, decodedStringProfile.length);

        String TimeAgo = getTimeAgo(announcement.getDate());



        holder.imgAnnouncement.setImageBitmap(decodedByte);

        holder.imgProfile.setImageBitmap(decodedByteProfile);
        holder.txtCreator.setText(announcement.getCreator());
        holder.txtTitle.setText(announcement.getTitle());
        if(announcement.getStreet() == null){
            holder.txtBarangay.setText("Barangay 178");
        } else {
            holder.txtBarangay.setText("for the residents of: "+announcement.getStreet());
        }

        holder.txtAnnouncementDate.setText(TimeAgo);
        holder.txtDesc.setText(announcement.getDesc());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class AnnouncementHolder extends RecyclerView.ViewHolder{

        private TextView txtCreator, txtTitle, txtBarangay, txtDate, txtDesc, txtAnnouncementDate;
        private CircleImageView imgProfile;
        private ImageView imgAnnouncement;

        public AnnouncementHolder(@NonNull View itemView) {
            super(itemView);
            txtCreator = itemView.findViewById(R.id.txtAnnouncementName);
            txtTitle = itemView.findViewById(R.id.txtAnnouncementTitle);
            txtBarangay = itemView.findViewById(R.id.txtAnnouncementStreet);
            txtDate = itemView.findViewById(R.id.txtAnnouncementDate);
            txtDesc = itemView.findViewById(R.id.txtAnnouncementDesc);
            txtAnnouncementDate = itemView.findViewById(R.id.txtAnnouncementDate);

            imgProfile = itemView.findViewById(R.id.imgAnnouncementProfile);


            imgAnnouncement = itemView.findViewById(R.id.imgAnnouncementPhoto);


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

}
