package com.example.edge.Adapter;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edge.Model.Comment;
import com.example.edge.Model.User;
import com.example.edge.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class CommentRecyclerAdapter extends RecyclerView.Adapter<CommentRecyclerAdapter.ViewHolder> {

    private List<Comment> commentList;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    public CommentRecyclerAdapter(List<Comment> commentList) {

        this.commentList = commentList;
        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String body = commentList.get(position).getBody();
        String user = commentList.get(position).getUser_id();
        String timestamp = commentList.get(position).getTimestamp() + "";
        String post = commentList.get(position).getPost_id();
        String comment = commentList.get(position).getComment_id();
        holder.setBody(body);
        holder.setUserImage(user);
        holder.setUserText(user);
        holder.deleteComment(comment, user);
    }

    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView listCommentBody, listCommentName;
        private ImageView listCommentImage;
        private Button commentDeleteButton;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
        public void setBody(String text) {
            listCommentBody = itemView.findViewById(R.id.listCommentBody);
            listCommentBody.setText(text);
        }
        public void setUserText(String text) {
            firebaseFirestore.collection("users").document(text).get().
                    addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(@NonNull DocumentSnapshot documentSnapshot) {
                            String username = (String) documentSnapshot.get("username");
                            listCommentName = itemView.findViewById(R.id.listCommentName);
                            listCommentName.setText(username);
                        }
                    });

        }
        public void setUserImage(String text) {
            listCommentImage = itemView.findViewById(R.id.postUserImage);
            final StorageReference imageReference = storageReference.child("profile_images").child(text+".jpg");
            try {
                final File localFile = File.createTempFile("images", "jpg");
                imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Uri uri = Uri.parse(localFile.toString());
                        if (uri != null) {
                            Log.d("null:", uri.toString());
//                            listCommentImage.setImageURI(uri);
                        }
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void deleteComment(final String text, final String user) {
            commentDeleteButton = itemView.findViewById(R.id.commentDeleteButton);
            if (mAuth.getUid().equals(user)) {
                commentDeleteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        firebaseFirestore.collection("comments").document(text).delete();
                    }
                });
            }
            else {
                commentDeleteButton.setVisibility(View.GONE);
            }
        }

    }

}
