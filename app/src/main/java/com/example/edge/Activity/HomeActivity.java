package com.example.edge.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.example.edge.Adapter.FragmentAdapter;
import com.example.edge.Fragment.ChatHeaderFragment;
import com.example.edge.Fragment.FriendFragment;
import com.example.edge.Fragment.OtherFragment;
import com.example.edge.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeActivity extends AppCompatActivity {

    ViewPager viewPager;
    BottomNavigationView navigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        navigation = findViewById(R.id.navigation);
        viewPager = findViewById(R.id.viewPager);
        setupFm(getSupportFragmentManager(), viewPager);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new PageChange());
    }

    public static void setupFm(FragmentManager fragmentManager, ViewPager viewPager) {
        FragmentAdapter fragmentAdapter = new FragmentAdapter(fragmentManager);
        fragmentAdapter.add(new FriendFragment(), "User");
        fragmentAdapter.add(new ChatHeaderFragment(), "Chat");
        fragmentAdapter.add(new OtherFragment(),"Other");
        Log.d("Size",fragmentAdapter.getCount()+"");
        viewPager.setAdapter(fragmentAdapter);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.chat:
                    viewPager.setCurrentItem(0);
                    return true;
                case R.id.user:
                    viewPager.setCurrentItem(1);
                    return true;
                case R.id.other:
                    viewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        }

    };

    public class PageChange implements  ViewPager.OnPageChangeListener{

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            switch(position){
                case 0:
                    navigation.setSelectedItemId(R.id.chat);
                    break;
                case 1:
                    navigation.setSelectedItemId(R.id.user);
                    break;
                case 2:
                    navigation.setSelectedItemId(R.id.other);
                    break;
            }

        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

    }

}
