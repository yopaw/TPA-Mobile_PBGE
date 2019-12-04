package com.example.edge.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.edge.Model.User;
import com.example.edge.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.Result;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class ScanQrActivity extends AppCompatActivity{

    FirebaseFirestore firestore;
    TextView idTxt,notFoundTxt;
    ImageView profileView;

    FirebaseStorage storage;
    StorageReference reference;
    Button addFriendBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);
        addFriendBtn = findViewById(R.id.addBtn);
        idTxt = findViewById(R.id.idTxt);
        notFoundTxt = findViewById(R.id.notFoundTxt);
        profileView = findViewById(R.id.profile);
        IntentIntegrator integrator = new IntentIntegrator(ScanQrActivity.this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
        integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(false);
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode,resultCode,data);
        if(result != null){
            if(result.getContents() == null) Toast.makeText(this, "Cancelled Scanning", Toast.LENGTH_SHORT).show();
            else {
                firestore = FirebaseFirestore.getInstance();
                firestore.collection("users").document(result.getContents()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            storage = FirebaseStorage.getInstance();
                            DocumentSnapshot documentSnapshot = task.getResult();
                            final User user = documentSnapshot.toObject(User.class);
                            if(user != null){
                                reference = storage.getReference().child("profile"+user.getUID());
                                reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if(task.isSuccessful()){
                                            if(user != null){
                                                notFoundTxt.setVisibility(View.INVISIBLE);
                                                Picasso.with(ScanQrActivity.this).load(task.getResult()).into(profileView);
                                                profileView.setVisibility(View.VISIBLE);
                                                idTxt.setText(user.getId());
                                                idTxt.setVisibility(View.VISIBLE);
                                                addFriendBtn.setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        SharedPreferences sharedPreferences = getSharedPreferences("preference", Context.MODE_PRIVATE);
                                                        final String uid = sharedPreferences.getString("uid","");
                                                        firestore.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if(task.isSuccessful()){
                                                                    ArrayList<String> friends = new ArrayList<>();
                                                                    DocumentSnapshot documentSnapshot = task.getResult();
                                                                    User currUser = documentSnapshot.toObject(User.class);
                                                                    friends = currUser.getFriendsUid();
                                                                    if(!friends.contains(user.getUID())) {
                                                                        addFriendBtn.setVisibility(View.VISIBLE);
                                                                        friends.add(user.getUID());
                                                                        firestore.collection("users").document(uid).update("friendsUid",friends).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                                Toast.makeText(ScanQrActivity.this, "Friend Added", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        });
                                                                    }
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                            }

                                        }
                                    }
                                });
                            }
                            else{
                                addFriendBtn.setVisibility(View.INVISIBLE);
                                profileView.setVisibility(View.INVISIBLE);
                                idTxt.setVisibility(View.INVISIBLE);
                                notFoundTxt.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
            }
        }
        else{
            super.onActivityResult(requestCode,resultCode,data);
        }

    }
}
