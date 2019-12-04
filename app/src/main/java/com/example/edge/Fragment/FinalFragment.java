package com.example.edge.Fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.edge.Activity.FinishRegister;
import com.example.edge.Activity.HomeActivity;
import com.example.edge.R;
import com.example.edge.Utils.Helper;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * A simple {@link Fragment} subclass.
 */
public class FinalFragment extends Fragment {

    Button readyBtn;
    FirebaseFirestore firestore;

    public FinalFragment() {
        makeProgress();
    }

    public void makeProgress(){
        if(Helper.progressNow < 100 && !Helper.FINAL_STATE){
            if(Helper.progressNow + 34 < 100){
                Helper.progressNow += 34;
                Helper.FINAL_STATE = true;
            }
            else{
                Helper.progressNow = 100;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_final, container, false);
        ((FinishRegister)getActivity()).continueProgress();
        readyBtn = view.findViewById(R.id.readyBtn);
        readyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firestore = FirebaseFirestore.getInstance();
                SharedPreferences preferences = getActivity().getSharedPreferences("preference",Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putInt("state",1);
                editor.apply();
                String uid = preferences.getString("uid","");
                DocumentReference documentReference = firestore.collection("users").document(uid);
                documentReference.update("id",Helper.id);
                documentReference.update("username",Helper.username);
                documentReference.update("state",1);
                Intent intent = new Intent(getContext(), HomeActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }

}
