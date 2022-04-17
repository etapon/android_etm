package com.example.e_taponmo.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.e_taponmo.Adapters.ComplaintAdapter;
import com.example.e_taponmo.Models.Complaint;
import com.example.e_taponmo.R;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class ComplaintForResident extends Fragment {
    private View view;
    private RecyclerView recyclerView;
    private ArrayList<Complaint> arrayList;
    private SwipeRefreshLayout refreshLayout;
    private ComplaintAdapter complaintAdapter;
    private MaterialToolbar toolbar;
    private SharedPreferences sharedPreferences;

    public ComplaintForResident(){

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_complaints, container, false);
//        init();
        return view;
    }
}
