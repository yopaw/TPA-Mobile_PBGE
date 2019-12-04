package com.example.edge.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.example.edge.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InsertPostActivity extends AppCompatActivity {

    private Button insertPostButton;
    private EditText insertPostEdit;
    private Switch publicSwitch;
    private ImageView insertPostImage;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_insert_post);
        insertPostButton = findViewById(R.id.insertPostButton);
        insertPostEdit= findViewById(R.id.insertPostEdit);
        publicSwitch = findViewById(R.id.publicSwitch);
        insertPostImage = findViewById(R.id.insertPostImage);

        mAuth = FirebaseAuth.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        insertPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> map = new HashMap<>();
                map.put("desc", insertPostEdit.getText().toString());
                map.put("user_id", mAuth.getUid());
                map.put("public", publicSwitch.isChecked());
                map.put("timestamp", FieldValue.serverTimestamp());
                firebaseFirestore.collection("posts").add(map).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        StorageReference imageReference = storageReference.child("post_images").child(documentReference.getId()+".jpg");
                        if (imageUri != null) {
                            imageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(InsertPostActivity.this, "Update Profile Success", Toast.LENGTH_LONG).show();
                                    }
                                    else {
                                        Toast.makeText(InsertPostActivity.this, "Update Profile Fail", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }

                    }
                });
            }
        });

        insertPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(InsertPostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(InsertPostActivity.this, "Permission Text", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(InsertPostActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                    else {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(InsertPostActivity.this);
                    }
                }

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                imageUri = result.getUri();
                insertPostImage.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
