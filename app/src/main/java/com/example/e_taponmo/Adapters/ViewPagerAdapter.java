package com.example.e_taponmo.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.example.e_taponmo.R;

public class ViewPagerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater inflater;

    public ViewPagerAdapter(Context context) {
        this.context = context;
    }

    private int images[]={
            R.drawable.p1,
            R.drawable.p2,
            R.drawable.p3
    };

    private String titles[]={
            "Innovation",
            "Segregate",
            "Manage"
    };

    private String descs[]={
            "Locate where your waste collectors location, see the announcements, collection schedules and more",
            "waste collection is more manageable, segregate waste for the environment, be GREEN!",
            "Collected waste data are stored and can be use in analytics"
    };

    @Override
    public int getCount() {
        return titles.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout)object;
    }
    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position){
        inflater = (LayoutInflater)context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.view_pager, container, false);

        ImageView imageView = v.findViewById(R.id.imgViewPager);
        TextView txtTitle = v.findViewById(R.id.txtTitleViewPager);
        TextView txtDesc = v.findViewById(R.id.txtDescViewPager);

        imageView.setImageResource(images[position]);
        txtTitle.setText(titles[position]);
        txtDesc.setText(descs[position]);

        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout)object);
    }
}
