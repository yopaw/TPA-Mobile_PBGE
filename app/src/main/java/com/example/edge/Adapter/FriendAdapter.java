package com.example.edge.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edge.Activity.ChatDetailActivity;
import com.example.edge.Model.ChatDetail;
import com.example.edge.Model.ChatHeader;
import com.example.edge.Model.User;
import com.example.edge.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private List<User> friends;
    private Context context;
    private List<ChatHeader> chatHeaders;
    DatabaseReference reference;

    public FriendAdapter(Context context, List<User> friends){
        this.friends = friends;
        this.context = context;
    }



    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_item,parent,false);

        return new FriendAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User friend = friends.get(position);
        Picasso.with(context).load(friend.getUrl()).into(holder.friendPicture);
        holder.friendUsernameTxt.setText(friend.getUsername());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Chat With "+friend.getUsername()+" ?");
                builder.setTitle("Make Chat");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SharedPreferences sharedPreferences = context.getSharedPreferences("preference",Context.MODE_PRIVATE);
                        String uid = sharedPreferences.getString("uid","");
                        readMessages(uid,friend.getUID());
                        Intent intent = new Intent(context, ChatDetailActivity.class);
                        intent.putExtra("friendUid",friend.getUID());
                        context.startActivity(intent);
                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    private void readMessages(final String id, final String friendUid){
        chatHeaders = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("ChatHeader");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatHeaders.clear();
                Log.wtf("ABC",dataSnapshot.toString());
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatHeader chat = snapshot.getValue(ChatHeader.class);

                    if(chat.getReceiver().equals(id) && chat.getSender().equals(friendUid) || chat.getReceiver().equals(friendUid) && chat.getSender().equals(id)){
                        chatHeaders.add(chat);
                    }
                }
                if(chatHeaders.size() == 0) createMessage(id,friendUid);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void createMessage(String sender,String receiver){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("ChatHeader");
        HashMap<String,Object> hashMap = new HashMap<>();
        String id = reference.push().getKey();
        hashMap.put("id",id);
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("archived",0);
        hashMap.put("favorite",0);
        reference.child(id).setValue(hashMap);

    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public  class ViewHolder extends  RecyclerView.ViewHolder{

        public ImageView friendPicture;

        public TextView friendUsernameTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            friendPicture = itemView.findViewById(R.id.friendPicture);
            friendUsernameTxt = itemView.findViewById(R.id.friendUsernameTxt);
        }
    }

}
