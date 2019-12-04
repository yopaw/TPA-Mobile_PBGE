package com.example.edge.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

public class UpdatePostActivity extends AppCompatActivity {

    private Button updatePostButton;
    private EditText updatePostEdit;
    private Switch updatePublicSwitch;
    private ImageView updatePostImage;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_post);

        final String post_id = getIntent().getStringExtra("POST_ID");

        updatePostButton = findViewById(R.id.updatePostButton);
        updatePostEdit = findViewById(R.id.updatePostEdit);
        updatePublicSwitch = findViewById(R.id.updatePublicSwitch);
        updatePostImage = findViewById(R.id.updatePostImage);

        mAuth = FirebaseAuth.getInstance();

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("posts").document(post_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc.exists()) {
                        Boolean publicity = (Boolean) doc.get("public");
                        String desc = (String) doc.get("desc");
                        updatePostEdit.setText(desc);
                        updatePublicSwitch.setChecked(publicity);
                    }
            }
        });

        updatePostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> map = new HashMap<>();
                map.put("desc", updatePostEdit.getText().toString());
                map.put("user_id", mAuth.getUid());
                map.put("public", updatePublicSwitch.isChecked());
                map.put("timestamp", FieldValue.serverTimestamp());

                firebaseFirestore.collection("posts").document(post_id).set(map, SetOptions.merge());
                StorageReference imageReference = storageReference.child("post_images").child(post_id + ".jpg");
                if (imageUri != null) {
                    imageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(UpdatePostActivity.this, "Update Profile Success", Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(UpdatePostActivity.this, "Update Profile Fail", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        updatePostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(UpdatePostActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(UpdatePostActivity.this, "Permission Text", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(UpdatePostActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    }
                    else {
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(UpdatePostActivity.this);
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
                updatePostImage.setImageURI(imageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}

