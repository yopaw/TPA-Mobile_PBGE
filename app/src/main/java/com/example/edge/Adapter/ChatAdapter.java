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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edge.Activity.ChatDetailActivity;
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

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private List<User> friends = new ArrayList<User>();
    private Context context;
    private List<ChatHeader> chatHeaders = new ArrayList<ChatHeader>();
    DatabaseReference reference;
    View dialogView;

    public ChatAdapter(Context context){
        this.context = context;
    }

    public void setStart(List<User> friends,List<ChatHeader> chatHeaders){
        this.friends.clear();
        this.friends.addAll(friends);
        this.chatHeaders.clear();
        this.chatHeaders.addAll(chatHeaders);
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.friend_item,parent,false);

        return new ChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final User friend = friends.get(position);
        Picasso.with(context).load(friend.getUrl()).into(holder.friendPicture);
        holder.friendUsernameTxt.setText(friend.getUsername());
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context,ChatDetailActivity.class);
                intent.putExtra("friendUid",friend.getUID());
                context.startActivity(intent);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                dialogView = LayoutInflater.from(context).inflate(R.layout.chat_form,null);
                builder.setView(dialogView);
                builder.setMessage("Chat With "+friend.getUsername()+" ?");
                builder.setTitle("Edit Chat");
                builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

              Switch favoriteSwitch = dialogView.findViewById(R.id.favoriteSwitch);
              Switch archiveSwitch = dialogView.findViewById(R.id.archivedSwitch);

              int favorite = 0,archive = 0;

                        if(favoriteSwitch.isChecked()){
                            favorite = 1;
                        }
                        if(archiveSwitch.isChecked()){
                            archive = 1;
                        }

                        SharedPreferences sharedPreferences = context.getSharedPreferences("preference",Context.MODE_PRIVATE);
                        final String uid = sharedPreferences.getString("uid","");
                       // readMessages(uid,friend.getUID(),position,archive,favorite);

                    }
                }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                return true;
            }
        });
    }

    private void updateData(String id,int archived, int favorite,int position){
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("ChatHeader").child(id);
        ChatHeader chatHeader = chatHeaders.get(position);
        chatHeader.setArchived(archived);
        chatHeader.setFavorite(favorite);
        databaseReference.setValue(chatHeader);
    }

    private void readMessages(final String id, final String friendUid, final int position, final int archive, final int favorite){
        chatHeaders = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("ChatHeader");
        chatHeaders.clear();
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatHeader chat = snapshot.getValue(ChatHeader.class);
                    Toast.makeText(context, ""+chat.getReceiver()+" "+chat.getSender(), Toast.LENGTH_SHORT).show();
                    if((chat.getReceiver().equals(id) && chat.getSender().equals(friendUid))
                            || (chat.getReceiver().equals(friendUid) && chat.getSender().equals(id))){
                        chatHeaders.add(chat);
                    }
                }
                if(!chatHeaders.isEmpty())updateData(chatHeaders.get(position).getId(),archive,favorite,position);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(context, "Canceled", Toast.LENGTH_SHORT).show();
            }
        });
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
