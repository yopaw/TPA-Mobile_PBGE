package com.example.edge.Adapter;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edge.Activity.PostActivity;
import com.example.edge.Activity.UpdatePostActivity;
import com.example.edge.Model.Post;
import com.example.edge.Model.User;
import com.example.edge.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {

    private List<Post> postList;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private AppCompatActivity activity;
    private List<String> likes;


    public PostRecyclerAdapter(List<Post> postList) {

        this.postList = postList;
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        likes = new ArrayList<>();
        firebaseFirestore.collection("users").document(mAuth.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                List<String> list = (List<String>) documentSnapshot.getData().get("likes");
                likes.clear();
                if (list != null) {
                    for (String val : list) {
                        likes.add(val);
                    }
                }
            }
        });
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String desc = postList.get(position).getDesc();
        String user = postList.get(position).getUser_id();
        String timestamp = postList.get(position).getTimestamp() + "";
        String post = postList.get(position).getPost_id();
        holder.setDesc(desc);
        holder.setUserImage(user);
        holder.setUserText(user);
        holder.likePost(post);
        holder.sharePost(post);
        holder.deletePost(post, user);
        holder.updatePost(post, user);
        holder.setImageCover(post);
        holder.setNavigation(post);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public void setCurrentActivity(AppCompatActivity activity) {
        this.activity = activity;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView postDescription, postUserText;
        private ImageView postUserImage;
        private Button postLikeButton, postShareButton, postDeleteButton, postUpdateButton;
        private ImageView postImageCover;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
        public void setDesc(String text) {
            postDescription = itemView.findViewById(R.id.postDescription);
            postDescription.setText(text);
        }
        public void setUserText(String text) {
            firebaseFirestore.collection("users").document(text).get().
                    addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                            String usrename = (String) documentSnapshot.get("username");
                            postUserText = itemView.findViewById(R.id.postUserText);
                            postUserText.setText(usrename);
                        }
                    });

        }
        public void setUserImage(String text) {
            postUserImage = itemView.findViewById(R.id.postUserImage);
            StorageReference imageReference = storageReference.child("profile"+text);
            try {
                final File localFile = File.createTempFile("images", "jpg");
                imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        postUserImage.setImageURI(Uri.parse(localFile.toString()));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void setImageCover(String text) {
            postImageCover = itemView.findViewById(R.id.postImageCover);
            StorageReference imageReference = storageReference.child("post_images").child(text);
            try {
                final File localFile = File.createTempFile("images", "jpg");
                imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        postImageCover.setImageURI(Uri.parse(localFile.toString()));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void likePost(final String post_id) {
            postLikeButton = itemView.findViewById(R.id.postLikeButton);
            if (likes.contains(post_id)) {
                postLikeButton.setText("Unlike");
                postLikeButton.setBackgroundColor(Color.parseColor("#3195B7"));
                postLikeButton.setTextColor(Color.parseColor("#FFFFFF"));
            }
            postLikeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("PostR", "LIKE");
                    if (!likes.contains(post_id)) {
                        firebaseFirestore.collection("users").document(mAuth.getUid()).
                                update("likes", FieldValue.arrayUnion(post_id)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
//                                postLikeButton.setText("Like");
//                                postLikeButton.setBackgroundColor(Color.parseColor("#FFFFFF"));
//                                postLikeButton.setTextColor(Color.parseColor("#000000"));
                            }
                        });
                    }
                    else {
                        firebaseFirestore.collection("users").document(mAuth.getUid()).
                                update("likes", FieldValue.arrayRemove(post_id)).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
//                                postLikeButton.setText("Unlike");
//                                postLikeButton.setBackgroundColor(Color.parseColor("#3195B7"));
//                                postLikeButton.setTextColor(Color.parseColor("#FFFFFF"));
                            }
                        });
                    }
                }
            });
        }
        public void deletePost(final String post_id, final String user_id) {
            postDeleteButton = itemView.findViewById(R.id.postDeleteButton);
            if (mAuth.getUid().equals(user_id)) {
                postDeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Log.d("PostR", "Delete");
                        firebaseFirestore.collection("posts").document(post_id).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                HashMap<String, Object> map = new HashMap<>();
                                map.put(post_id, FieldValue.delete());
                                firebaseFirestore.collection("users").document(mAuth.getUid()).update("likes", FieldValue.arrayRemove(post_id));
                            }
                        });
                    }
                });
            }
            else {
                postDeleteButton.setVisibility(View.GONE);
            }
        }
        public void sharePost(String post_id) {
            postShareButton = itemView.findViewById(R.id.postShareButton);
            postShareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
        public void updatePost(final String post_id, final String user_id) {
            postUpdateButton = itemView.findViewById(R.id.postUpdateButton);
            if (mAuth.getUid().equals(user_id)) {
                postUpdateButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(activity, UpdatePostActivity.class);
                        intent.putExtra("POST_ID", post_id);
                        activity.startActivity(intent);
                    }
                });
            }
            else {
                postUpdateButton.setVisibility(View.GONE);
            }

        }

        public void setNavigation(final String post_id) {
            itemView.findViewById(R.id.userPostCard).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, PostActivity.class);
                    intent.putExtra("POST_ID", post_id);
                    activity.startActivity(intent);
                }
            });
        }

    }

}
