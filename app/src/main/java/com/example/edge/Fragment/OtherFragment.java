package com.example.edge.Fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.example.edge.Activity.AddUserActvity;
import com.example.edge.Activity.FriendActivity;
import com.example.edge.Activity.LoginActivity;
import com.example.edge.Activity.MainActivity;
import com.example.edge.Activity.NewsActivity;
import com.example.edge.Activity.TimelineActivity;
import com.example.edge.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class OtherFragment extends Fragment {



    LinearLayout profileLayout,addFriendLayout,logoutLayout,newsLayout,friendLayout,timelineLayout;

    public OtherFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_other, container, false);
        profileLayout = view.findViewById(R.id.profileLayout);
        addFriendLayout = view.findViewById(R.id.userLayout);
        logoutLayout = view.findViewById(R.id.LogoutLayout);
        newsLayout = view.findViewById(R.id.newsLayout);
        friendLayout = view.findViewById(R.id.friendLayout);
        timelineLayout = view.findViewById(R.id.timelineLayout);

        profileLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), MainActivity.class);
                startActivity(intent);
            }
        });

        addFriendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), AddUserActvity.class);
                startActivity(intent);
            }
        });

        newsLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), NewsActivity.class);
                startActivity(intent);
            }
        });

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getContext().getSharedPreferences("preference", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();
                Intent intent = new Intent(getContext(), LoginActivity.class);
                startActivity(intent);
            }
        });

        friendLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), FriendActivity.class);
                startActivity(intent);
            }
        });

        timelineLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), TimelineActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

}
