package com.example.edge.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edge.Model.ChatDetail;
import com.example.edge.Model.User;
import com.example.edge.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    public  static final int MSG_TYPE_LEFT = 0;
    public  static final int MSG_TYPE_RIGHT = 1;
    String uid;

    private Context context;
    private List<ChatDetail> chats;
    private User user;
    FirebaseStorage firebaseStorage;
    StorageReference reference;

    public MessageAdapter(Context context,List<ChatDetail> chats){
        this.context = context;
        this.chats = chats;
        SharedPreferences sharedPreferences = context.getSharedPreferences("preference",Context.MODE_PRIVATE);
        uid = sharedPreferences.getString("uid","");
    }


    @NonNull
    @Override
    public MessageAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == MSG_TYPE_LEFT){
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_left,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.chat_item_right,parent,false);
            return new MessageAdapter.ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageAdapter.ViewHolder holder, int position) {
        ChatDetail chatDetail = chats.get(position);

        holder.show_message.setText(chatDetail.getMessage());
        holder.time.setText(chatDetail.getTime());
        firebaseStorage = FirebaseStorage.getInstance();
        if(uid.equals(chatDetail.getReceiver())){
            reference = firebaseStorage.getReference().child("profile"+chatDetail.getSender());
        }
        else if(uid.equals(chatDetail.getSender())){
            reference = firebaseStorage.getReference().child("profile"+chatDetail.getReceiver());
        }
        reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Picasso.with(context).load(task.getResult()).into(holder.chatIv);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView show_message,time;
        public CircleImageView chatIv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            show_message = itemView.findViewById(R.id.show_message);
            time = itemView.findViewById(R.id.time);
            chatIv = itemView.findViewById(R.id.profile);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(chats.get(position).getSender().equals(uid)) return MSG_TYPE_RIGHT;
        else return MSG_TYPE_LEFT;
    }
}

