package com.example.edge.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.edge.Adapter.PostRecyclerAdapter;
import com.example.edge.Model.Post;
import com.example.edge.Model.User;
import com.example.edge.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private ImageView profileImage;
    private EditText profileName, profileDesc;
    private Button editProfileButton, saveProfileButton, cancelProfileButton;
    private ProgressBar uploadProfileProgress;
    private RecyclerView postListView;
    private Toolbar toolbar;
    private int toolbarState = 0;
    private Uri profileImageUri = null;
    private boolean editMode = false;

    private Uri currentImageUri = null;
    private File localFile = null;

    private StorageReference storageReference;
    private List<Post> postList;
    private FirebaseFirestore firebaseFirestore;
    private PostRecyclerAdapter postRecyclerAdapter;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        postList = new ArrayList<>();
        postListView = findViewById(R.id.postListView);
        postRecyclerAdapter = new PostRecyclerAdapter(postList);
        postRecyclerAdapter.setCurrentActivity(this);
        postListView.setAdapter(postRecyclerAdapter);
        postListView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        Log.d("error", firebaseFirestore.toString());
        firebaseFirestore.collection("posts").whereEqualTo("user_id", mAuth.getUid()).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                postList.clear();
                for (DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()) {

                        String post_id = doc.getDocument().getId();
                        String desc = (String) doc.getDocument().getData().get("desc");
                        Timestamp timestamp = (Timestamp) doc.getDocument().getData().get("timestamp");
                        String user_id = (String) doc.getDocument().getData().get("user_id");

                        postList.add(new Post(user_id, desc, post_id, timestamp));
                        postRecyclerAdapter.notifyDataSetChanged();

                }
            }
        });

        profileImage = findViewById(R.id.profileImage);
        profileName = findViewById((R.id.profileName));
        profileDesc = findViewById(R.id.profiledDesc);

        firebaseFirestore.collection("users").document(mAuth.getUid()).get().
                addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        String username = (String) documentSnapshot.get("username");
                        String desc = (String) documentSnapshot.get("desc");
                        profileName.setText(username);
                        profileDesc.setText(desc);
                    }
                });

        uploadProfileProgress = findViewById(R.id.uploadProfileProgress);

        StorageReference imageReference = storageReference.child("profile"+mAuth.getUid());
        try {
            localFile = File.createTempFile("images", "jpg");
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                profileImage.setImageURI(Uri.parse(localFile.toString()));
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            if (!editMode) {
                return;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission Text", Toast.LENGTH_LONG).show();
                    ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                }
                else {
                    CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(MainActivity.this);
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
                profileImageUri = result.getUri();
                Log.d(MainActivity.class.getSimpleName(), profileImageUri.toString());
                profileImage.setImageURI(profileImageUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        if (toolbarState == 0) {
            menu.findItem(R.id.save).setVisible(false);
            menu.findItem(R.id.cancel).setVisible(false);
        }
        else if (toolbarState == 1) {
            editMode = !editMode;
            profileName.setEnabled(editMode);
            profileDesc.setEnabled(editMode);
            menu.findItem(R.id.edit).setVisible(false);
            menu.findItem(R.id.save).setVisible(true);
            menu.findItem(R.id.cancel).setVisible(true);
        }
        else if (toolbarState == 2) {
            Map<String, Object> user = new HashMap<>();
            user.put("username", profileName.getText().toString());
            user.put("desc", profileDesc.getText().toString());

            firebaseFirestore.collection("users").document(mAuth.getUid())
                    .set(user, SetOptions.merge());
            StorageReference imageReference = storageReference.child("profile" + mAuth.getUid());
            uploadProfileProgress.setVisibility(View.VISIBLE);
            if (profileImageUri != null) {
                imageReference.putFile(profileImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        uploadProfileProgress.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()) {
                            editMode = !editMode;
                            profileName.setEnabled(editMode);
                            profileDesc.setEnabled(editMode);
                            menu.findItem(R.id.edit).setVisible(true);
                            menu.findItem(R.id.save).setVisible(false);
                            menu.findItem(R.id.cancel).setVisible(false);
                            Toast.makeText(MainActivity.this, "Update Profile Success", Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(MainActivity.this, "Update Profile Fail", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            else {
                uploadProfileProgress.setVisibility(View.VISIBLE);
                editMode = !editMode;
                profileName.setEnabled(editMode);
                profileDesc.setEnabled(editMode);
                menu.findItem(R.id.edit).setVisible(true);
                menu.findItem(R.id.save).setVisible(false);
                menu.findItem(R.id.cancel).setVisible(false);
                Toast.makeText(MainActivity.this, "Update Profile Success", Toast.LENGTH_LONG).show();
            }

        }
        else if (toolbarState == 3) {
            editMode = !editMode;
            profileName.setEnabled(editMode);
            profileDesc.setEnabled(editMode);
            menu.findItem(R.id.edit).setVisible(true);
            menu.findItem(R.id.save).setVisible(false);
            menu.findItem(R.id.cancel).setVisible(false);
            profileImage.setImageURI(Uri.parse(localFile.toString()));
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit: {
                toolbarState = 1;
                break;
            }
            case R.id.save: {
                toolbarState = 2;
                break;
            }
            case R.id.cancel: {
                toolbarState = 3;
                break;
            }

        }
        invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }
}
