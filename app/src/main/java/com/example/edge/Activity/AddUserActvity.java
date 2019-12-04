package com.example.edge.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.example.edge.Adapter.TabAdapter;
import com.example.edge.Fragment.AddUserByQrCode;
import com.example.edge.Fragment.AddUserBySearch;
import com.example.edge.R;
import com.google.android.material.tabs.TabLayout;

public class AddUserActvity extends AppCompatActivity {

    private TabLayout tabLayout;
    private TabAdapter tabAdapter;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_actvity);
        tabLayout = findViewById(R.id.tabLayout);
        tabAdapter = new TabAdapter(getSupportFragmentManager());
        viewPager = findViewById(R.id.viewPager);
        tabAdapter.add(new AddUserBySearch(),"Add By Search");
        tabAdapter.add(new AddUserByQrCode(),"Add By Qr Code");

        viewPager.setAdapter(tabAdapter);
        tabLayout.setupWithViewPager(viewPager);

    }
}
