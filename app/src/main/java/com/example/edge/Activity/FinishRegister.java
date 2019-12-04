package com.example.edge.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.edge.Fragment.FinalFragment;
import com.example.edge.Fragment.InfoFragment;
import com.example.edge.Fragment.PhotoFragment;
import com.example.edge.R;
import com.example.edge.Utils.Helper;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FinishRegister extends AppCompatActivity {

    BottomNavigationView navigationView;
    ProgressBar progressBar;
    public int mProgressStatus = 0;
    MenuItem photoFragment,finalFragment;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_register);
        progressBar = findViewById(R.id.progress);
        Toast.makeText(this, "Masuk pak Eko", Toast.LENGTH_SHORT).show();

        navigationView = findViewById(R.id.bottomNavigation);
        photoFragment = navigationView.getMenu().findItem(R.id.navigationPhoto);
        navigationView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        InfoFragment fragment = new InfoFragment();
        replaceFragment(fragment);
    }

    public void continueProgress(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mProgressStatus < Helper.progressNow){
                    mProgressStatus++;
                    android.os.SystemClock.sleep(50);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress(mProgressStatus);
                        }
                    });
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }).start();
    }

    BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch(menuItem.getItemId()){
                case R.id.navigationInformation:
                    InfoFragment infoFragment = new InfoFragment();
                    replaceFragment(infoFragment);
                    return true;
                case R.id.navigationPhoto:
                    if(!Helper.continueFlag)
                        Toast.makeText(FinishRegister.this, "Please Fill your Info First", Toast.LENGTH_SHORT).show();
                    else{
                        PhotoFragment photoFragment = new PhotoFragment();
                        replaceFragment(photoFragment);
                    }
                    return true;
                case R.id.navigationFinal:
                    if(Helper.continueFlag){
                        FinalFragment finalFragment = new FinalFragment();
                        replaceFragment(finalFragment);
                    }
                    else{
                        Toast.makeText(FinishRegister.this, "Please Fill Your Info First", Toast.LENGTH_SHORT).show();
                    }
                    return true;
            }
            return false;
        }
    };

    void replaceFragment(Fragment fragment){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.replace(R.id.fl,fragment);
//        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

}
