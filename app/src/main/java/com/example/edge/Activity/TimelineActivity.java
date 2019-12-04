package com.example.edge.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.example.edge.Adapter.PostRecyclerAdapter;
import com.example.edge.Model.Post;
import com.example.edge.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TimelineActivity extends AppCompatActivity {

    private Toolbar timelineToolbar;

    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private PostRecyclerAdapter postRecyclerAdapter, likeRecyclerAdapter;
    private RecyclerView postListView, likeListView;
    private FirebaseAuth mAuth;
    private FloatingActionButton floatingInsertPostButton;

    private List<Post> postList;
    private List<Post> likedList;
    private List<String> friendList;
    private List<String> likeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        timelineToolbar = findViewById(R.id.timelineToolbar);
        setSupportActionBar(timelineToolbar);

        mAuth = FirebaseAuth.getInstance();

        postList = new ArrayList<>();
        friendList = new ArrayList<>();
        likeList = new ArrayList<>();
        likedList = new ArrayList<>();

        floatingInsertPostButton = findViewById(R.id.floatingInsertPostButton);
        floatingInsertPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TimelineActivity.this, InsertPostActivity.class);
                TimelineActivity.this.startActivity(intent);
            }
        });

        postListView = findViewById(R.id.friendPostListView);
        postRecyclerAdapter = new PostRecyclerAdapter(postList);
        postRecyclerAdapter.setCurrentActivity(this);
        postListView.setAdapter(postRecyclerAdapter);
        postListView.setLayoutManager(new LinearLayoutManager(this));

        likeListView = findViewById(R.id.friendLikeListView);
        likeListView.setVisibility(View.GONE);
        likeRecyclerAdapter = new PostRecyclerAdapter(likedList);
        likeRecyclerAdapter.setCurrentActivity(this);
        likeListView.setAdapter(likeRecyclerAdapter);
        likeListView.setLayoutManager(new LinearLayoutManager(this));

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("users").document(mAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                List<String> list = (List<String>) documentSnapshot.get("friendsUid");
                if (list != null) {
                    friendList.clear();
                    for (String val : list) {
                        friendList.add(val);
                    }
                    firebaseFirestore.collection("posts").whereIn("user_id", friendList).addSnapshotListener(new EventListener<QuerySnapshot>() {
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
                }
            }
        });

        firebaseFirestore.collection("users").document(mAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                List<String> list = (List<String>) documentSnapshot.get("likes");
                if (list != null) {
                    likedList.clear();
                    for (String val : list) {
                        Log.d("like", val);
                        firebaseFirestore.collection("posts").document(val).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot doc = task.getResult();
                                    if (doc.exists()) {
                                        String post_id = doc.getId();
                                        String desc = (String) doc.get("desc");
                                        Timestamp timestamp = (Timestamp) doc.get("timestamp");
                                        String user_id = (String) doc.get("user_id");

                                        likedList.add(new Post(user_id, desc, post_id, timestamp));
                                        likeRecyclerAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.friendOnly: {
                postListView.setVisibility(View.VISIBLE);
                likeListView.setVisibility(View.GONE);
                break;
            }
            case R.id.likeOnly: {
                postListView.setVisibility(View.GONE);
                likeListView.setVisibility(View.VISIBLE);
                break;
            }

        }
        invalidateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }
}
