package com.example.edge.Fragment;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.edge.Activity.FinishRegister;
import com.example.edge.R;
import com.example.edge.Utils.Helper;
import com.google.rpc.Help;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class InfoFragment extends Fragment {

    EditText usernameTxt,idTxt;
    CalendarView cvDOB;
    Button submitBtn;
    public InfoFragment() {

    }

    public void makeProgres(){
        if(Helper.progressNow < 100 && !Helper.INFO_STATE){
            if(Helper.progressNow + 33 < 100){
                Helper.progressNow += 33;
                Helper.INFO_STATE = true;
            }
            else{
                Helper.progressNow = 100;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.fragment_info, container, false);

        usernameTxt = view.findViewById(R.id.usernameTxt);
        idTxt = view.findViewById(R.id.idTxt);
        cvDOB = view.findViewById(R.id.calendarDOB);
        submitBtn = view.findViewById(R.id.submitBtn);
        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameTxt.getText().toString();
                String id = idTxt.getText().toString();
                Helper.continueFlag = validateInput(username,id);
            }
        });

        return view;
    }

   public boolean validateInput(String username,String id){
        Date userDOB;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String selectedDate = sdf.format(new Date(cvDOB.getDate()));
        Date currentDate = new Date();
        if(username.equals("")){
            Toast.makeText(getView().getContext(), "Username must be filled!", Toast.LENGTH_SHORT).show();
            return false;
        }
        else if(id.equals("")){
            Toast.makeText(getView().getContext(), "Id must be filled!", Toast.LENGTH_SHORT).show();
            return false;
        }
        Helper.id = id;
        Helper.username = username;
        makeProgres();
       ((FinishRegister)getActivity()).continueProgress();
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        usernameTxt.setText(Helper.username);
        idTxt.setText(Helper.id);
    }
}
