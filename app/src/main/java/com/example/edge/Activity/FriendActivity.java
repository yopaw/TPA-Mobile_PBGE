package com.example.edge.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.edge.Adapter.FriendRecyclerAdapter;
import com.example.edge.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FriendActivity extends AppCompatActivity {

    private Dialog dialog;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FriendRecyclerAdapter friendRecyclerAdapter, blockRecyclerAdapter, favoriteRecyclerAdapter;
    private RecyclerView friendListView, blockListView, favoriteListView;
    private List<String> friendList, blockList, favoriteList;
    private EditText searchFriendEdit;
//    private Button searchFriendButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        dialog = new Dialog(this);

        mAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

//        searchFriendButton = findViewById(R.id.searchFriendButton);
        searchFriendEdit = findViewById(R.id.searchFriendEdit);

        friendList = new ArrayList<>();
        friendListView = findViewById(R.id.friendListView);
        blockList = new ArrayList<>();
        blockListView = findViewById(R.id.blockListView);
        favoriteList = new ArrayList<>();
        favoriteListView = findViewById(R.id.favoriteListView);

        friendRecyclerAdapter = new FriendRecyclerAdapter(friendList, this);
        friendListView.setAdapter(friendRecyclerAdapter);
        friendListView.setLayoutManager(new LinearLayoutManager(FriendActivity.this));

        firebaseFirestore.collection("users").document(mAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                List<String> list = (List<String>)documentSnapshot.getData().get("friendsUid");
                friendList.clear();
                if (list != null) {
                    for (String value: list) {
                        if (value.contains(searchFriendEdit.getText().toString())) {
                            friendList.add(value);
                            friendRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

        searchFriendEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                firebaseFirestore.collection("users").document(mAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        List<String> list = (List<String>)documentSnapshot.get("friendsUid");
                        friendList.clear();
                        friendRecyclerAdapter.notifyDataSetChanged();
                        if (list != null) {
                            for (final String value: list) {
                                firebaseFirestore.collection("users").document(value).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        String un = (String) documentSnapshot.get("username");
                                        if (un.contains(searchFriendEdit.getText().toString())) {
                                            friendList.add(value);
                                            friendRecyclerAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });

                            }
                        }
                    }
                });
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        blockRecyclerAdapter = new FriendRecyclerAdapter(blockList, this);
        blockListView.setAdapter(blockRecyclerAdapter);
        blockListView.setLayoutManager(new LinearLayoutManager(FriendActivity.this));

        firebaseFirestore.collection("users").document(mAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                List<String> list = (List<String>)documentSnapshot.getData().get("blocks");
                blockList.clear();
                if (list != null) {
                    for (String value: list) {
                        blockList.add(value);
                        blockRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        favoriteRecyclerAdapter = new FriendRecyclerAdapter(favoriteList, this);
        favoriteListView.setAdapter(favoriteRecyclerAdapter);
        favoriteListView.setLayoutManager(new LinearLayoutManager(FriendActivity.this));

        firebaseFirestore.collection("users").document(mAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                List<String> list = (List<String>)documentSnapshot.getData().get("favorites");
                favoriteList.clear();
                if (list != null) {
                    for (String value: list) {
                        favoriteList.add(value);
                        favoriteRecyclerAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

    }
    public void ShowPopup(final String user_id) {

        DocumentReference friendRef = firebaseFirestore.collection("users").document(user_id);
        final DocumentReference userRef = firebaseFirestore.collection("users").document(mAuth.getUid());

        final TextView closeButton, friendName, friendDescription;
        final LinearLayout blockButton, favoriteButton, friendButton;
        final ImageView friendImage;
        final TextView blockText, friendText, favoriteText;

        dialog.setContentView(R.layout.friend_popup);
        closeButton = dialog.findViewById(R.id.closePopupButton);
        closeButton.setText("  ");
        friendName = dialog.findViewById(R.id.friendName);
        friendDescription = dialog.findViewById(R.id.friendDescription);
        friendImage = dialog.findViewById(R.id.friendImage);

        blockButton = dialog.findViewById(R.id.blockButton);
        favoriteButton = dialog.findViewById(R.id.favoriteButton);
        friendButton = dialog.findViewById(R.id.friendButton);

        blockText = dialog.findViewById(R.id.blockText);
        friendText = dialog.findViewById(R.id.friendText);
        favoriteText = dialog.findViewById(R.id.favoriteText);


        friendRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String descText = (String)documentSnapshot.getData().get("desc");
                String nameText = (String)documentSnapshot.getData().get("name");
                friendName.setText(nameText);
                friendDescription.setText(descText);
                StorageReference imageReference = storageReference.child("profile"+user_id);
                try {
                    final File localFile = File.createTempFile("images", "jpg");
                    imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            friendImage.setImageURI(Uri.parse(localFile.toString()));
                        }
                    });
                } catch (IOException err) {
                    err.printStackTrace();
                }
            }
        });
        if (!blockList.contains(user_id)) {
            blockText.setText("Block");
        }
        else {
            blockText.setText("Unblock");
        }
        if (!favoriteList.contains(user_id)) {
            favoriteText.setText("Favorite");
        }
        else {
            favoriteText.setText("Unfav");
        }
        if (!friendList.contains(user_id)) {
            friendText.setText("Add");
        }
        else {
            friendText.setText("Unfriend");
        }
        blockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (blockList.contains(user_id)) {
                    userRef.update("blocks", FieldValue.arrayRemove(user_id));
                    blockText.setText("Block");
                }
                else {
                    userRef.update("blocks", FieldValue.arrayUnion(user_id));
                    blockText.setText("Unblock");
                }

            }
        });
        favoriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (favoriteList.contains(user_id)) {
                    userRef.update("favorites", FieldValue.arrayRemove(user_id));
                    favoriteText.setText("Favorite");
                }
                else {
                    userRef.update("favorites", FieldValue.arrayUnion(user_id));
                    favoriteText.setText("Unfav");
                }

            }
        });
        friendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (friendList.contains(user_id)) {
                    userRef.update("friends", FieldValue.arrayRemove(user_id));
                    friendText.setText("Add Friend");
                }
                else {
                    userRef.update("friends", FieldValue.arrayUnion(user_id));
                    friendText.setText("Unfriend");
                }
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.show();
    }
}
