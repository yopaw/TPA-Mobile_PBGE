package com.example.edge.Activity;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.edge.Adapter.CommentRecyclerAdapter;
import com.example.edge.Model.Comment;
import com.example.edge.R;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostActivity extends AppCompatActivity {

    private CommentRecyclerAdapter commentRecyclerAdapter;
    private RecyclerView commentListView;
    private List<Comment> commentList;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private Button commentButton;
    private EditText commentEdit;
    private TextView postUser, postDesc;

    private String post_id;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        post_id = getIntent().getStringExtra("POST_ID");
        mAuth = FirebaseAuth.getInstance();
        final String post_id = getIntent().getStringExtra("POST_ID");

        postUser = findViewById(R.id.postUser);
        postDesc = findViewById(R.id.postDesc);

        commentButton = findViewById(R.id.commentButton);
        commentEdit = findViewById(R.id.commentEdit);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        commentList = new ArrayList<>();
        commentListView = findViewById(R.id.commentListView);
        commentRecyclerAdapter = new CommentRecyclerAdapter(commentList);
        commentListView.setAdapter(commentRecyclerAdapter);
        commentListView.setLayoutManager(new LinearLayoutManager(PostActivity.this));

        firebaseFirestore.collection("posts").document(post_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                String desc = (String) documentSnapshot.get("desc");
                String user = (String) documentSnapshot.get("user_id");

                firebaseFirestore.collection("users").document(user).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                        String username = documentSnapshot.get("name").toString();
                        postUser.setText(username);

                    }
                });

                postDesc.setText(desc);
            }
        });

        firebaseFirestore.collection("comments").whereEqualTo("post_id", post_id).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                commentList.clear();
                for (DocumentChange doc:queryDocumentSnapshots.getDocumentChanges()) {
                    String comment_id = doc.getDocument().getId();
                    String post_id = (String) doc.getDocument().getData().get("post_id");
                    String body = (String) doc.getDocument().getData().get("body");
                    Timestamp timestamp = (Timestamp) doc.getDocument().getData().get("timestamp");
                    String user_id = (String) doc.getDocument().getData().get("user_id");

                    commentList.add(new Comment(user_id, comment_id, post_id, body, timestamp));
                    commentRecyclerAdapter.notifyDataSetChanged();
                }
            }
        });

        commentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Map<String, Object> data = new HashMap<>();
                data.put("body", commentEdit.getText().toString());
                data.put("user_id", mAuth.getUid());
                data.put("timestamp", new Timestamp(new Date()));
                data.put("post_id", post_id);
                firebaseFirestore.collection("comments").add(data);
            }
        });

    }
}
