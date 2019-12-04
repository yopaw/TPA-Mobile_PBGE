package com.example.edge.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.edge.Model.User;
import com.example.edge.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    FirebaseAuth auth;
    EditText emailTxt,passwordTxt;
    Button regisBtn,loginBtn;
    FirebaseFirestore fs;
    int state = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        auth = FirebaseAuth.getInstance();
        emailTxt = findViewById(R.id.emailTxt);
        passwordTxt = findViewById(R.id.passwordTxt);
        regisBtn  = findViewById(R.id.regisBtn);
        loginBtn = findViewById(R.id.loginBtn);
        fs = FirebaseFirestore.getInstance();
        SharedPreferences preferences = getSharedPreferences("preference",MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
//        editor.clear();
        editor.putInt("state",1);
        editor.apply();
//        editor.apply();
        String uid = preferences.getString("uid","");
        Toast.makeText(this, ""+uid, Toast.LENGTH_SHORT).show();
        if(!uid.equals("")){
            state = preferences.getInt("state",0);
//            state = 1;
            if(!uid.equals("")){
                Toast.makeText(RegisterActivity.this, "State "+state, Toast.LENGTH_SHORT).show();
                if(state == 0){
                    Intent intent = new Intent(RegisterActivity.this,FinishRegister.class);
                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent = new Intent(RegisterActivity.this,HomeActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
        Toast.makeText(this, ""+uid, Toast.LENGTH_SHORT).show();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);
                startActivity(intent );
//                CollectionReference users  = fs.collection("users");
//                users.document("wEoaBXdMxwY3rlc6QNW8xJMEU203").update("name","boom").addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Toast.makeText(RegisterActivity.this, "Boom", Toast.LENGTH_SHORT).show();
//                    }
//                });
            }
        });


        regisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String email = emailTxt.getText().toString();
                String password = passwordTxt.getText().toString();

                auth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(RegisterActivity.this, "Register Failed", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Map<String, User> newUser = new HashMap<>();
                            User user = new User();
                            user.setUID(auth.getUid());
                            user.setEmail(email);
                            newUser.put("User",user);
                            ArrayList<String> friendsUid = new ArrayList<>();
                            friendsUid.add("6NQLkBjPRlUVfrwJ9HMoJFzlPHH3");
                            friendsUid.add("rN4AOAXnnzPYM9TLy1FgEUmYlBG2");
                            user.setFriendsUid(friendsUid);
                            CollectionReference users  = fs.collection("users");
//                            users.document(auth.getUid()).update("name","boom").addOnSuccessListener(new OnSuccessListener<Void>() {
//                                @Override
//                                public void onSuccess(Void aVoid) {
//                                    Toast.makeText(RegisterActivity.this, "Boom", Toast.LENGTH_SHORT).show();
//                                }
//                            });
                            users.document(""+auth.getUid()).set(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    Log.e("InsertUser","User Inserted with ID"+auth.getUid());
                                    Toast.makeText(RegisterActivity.this, auth.getUid().toString(), Toast.LENGTH_SHORT).show();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.e("InsertUser","Error Insert User");
                                    Toast.makeText(RegisterActivity.this, auth.getUid().toString(), Toast.LENGTH_SHORT).show();
                                }
                            });
//                            users.add(user).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
//                                @Override
//                                public void onSuccess(DocumentReference documentReference) {
//                                    Log.e("InsertUser","User Inserted with ID"+auth.getUid());
//                                    Toast.makeText(RegisterActivity.this, auth.getUid().toString(), Toast.LENGTH_SHORT).show();
//                                }
//                            }).addOnFailureListener(new OnFailureListener() {
//                                @Override
//                                public void onFailure(@NonNull Exception e) {
//                                    Log.e("InsertUser","Error Insert User");
//                                    Toast.makeText(RegisterActivity.this, auth.getUid().toString(), Toast.LENGTH_SHORT).show();
//                                }
//                            });
                            Toast.makeText(RegisterActivity.this, auth.getUid().toString(), Toast.LENGTH_SHORT).show();
                            Toast.makeText(RegisterActivity.this, "Register Success", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }

    private void getUserState(final String uid){
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        firestore.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    User user;
                    DocumentSnapshot documentSnapshot = task.getResult();
                    user =  documentSnapshot.toObject(User.class);
                    Toast.makeText(RegisterActivity.this, "User State"+user.getState(), Toast.LENGTH_SHORT).show();
                    state = user.getState();

                }
                else{
                    Toast.makeText(RegisterActivity.this, "Gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

}
