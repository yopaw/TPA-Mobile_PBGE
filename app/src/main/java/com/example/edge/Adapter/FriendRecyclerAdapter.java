package com.example.edge.Adapter;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edge.Activity.FriendActivity;
import com.example.edge.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class FriendRecyclerAdapter extends RecyclerView.Adapter<FriendRecyclerAdapter.ViewHolder> {

    private List<String> friendList;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference;
    private FriendActivity friendActivity;
    public FriendRecyclerAdapter(List<String> friendList, FriendActivity friendActivity) {
        this.friendList = friendList;
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        this.friendActivity = friendActivity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.friend_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final String user_id = friendList.get(position);
        firebaseFirestore.collection("users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    String name = (String)document.getData().get("username");
                    holder.setUserImage(user_id);
                    holder.setUsername(name);
                    holder.popup(user_id);
                } else {
                    Log.d("Err", "get failed with ", task.getException());
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private View itemView;
        private TextView profileName;
        private ImageView profileImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            this.itemView = itemView;
        }
        public void popup(final String text) {
            CardView listFriendCard = itemView.findViewById(R.id.listFriendCard);
            listFriendCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("ClickErr", "TEST");
                    FriendRecyclerAdapter.this.friendActivity.ShowPopup(text);
                }
            });

        }
        public void setUsername(String text) {
            profileName = itemView.findViewById(R.id.listFriendName);
            profileName.setText(text);
        }
        public void setUserImage(String text) {
            profileImage = itemView.findViewById(R.id.listFriendImage);
            StorageReference imageReference = storageReference.child("profile" + text);
            try {
                final File localFile = File.createTempFile("images", "jpg");
                imageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        profileImage.setImageURI(Uri.parse(localFile.toString()));
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
