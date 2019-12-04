package com.example.edge.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.edge.Adapter.MessageAdapter;
import com.example.edge.Model.ChatDetail;
import com.example.edge.Model.User;
import com.example.edge.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatDetailActivity extends AppCompatActivity {

    CircleImageView imageView;
    TextView username;
    DatabaseReference reference;
    Intent intent;
    ImageButton btn_send;
    EditText text_send;
    MessageAdapter messageAdapter;
    List<ChatDetail> chatDetails;


    String uid;
    RecyclerView recyclerView;

    FirebaseFirestore firestore;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_detail);

        btn_send = findViewById(R.id.btn_send);
        text_send = findViewById(R.id.sendText);
        imageView = findViewById(R.id.profile);
        SharedPreferences sharedPreferences = getSharedPreferences("preference",MODE_PRIVATE);
        uid = sharedPreferences.getString("uid","");
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getApplicationContext());
        linearLayout.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayout);
        username = findViewById(R.id.username);


        firestore = FirebaseFirestore.getInstance();
        intent = getIntent();
        final String friendUid = intent.getStringExtra("friendUid");

        firestore.collection("users").document(friendUid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    User user = documentSnapshot.toObject(User.class);
                    username.setText(user.getUsername());

                    firebaseStorage = FirebaseStorage.getInstance();
                    storageReference = firebaseStorage.getReference().child("profile"+uid);
                    storageReference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if(task.isSuccessful()){
                                Picasso.with(ChatDetailActivity.this).load(task.getResult()).into(imageView);
                            }
                        }
                    });
                }
            }
        });

        reference = FirebaseDatabase.getInstance().getReference().child("users").child(friendUid);

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date date = new Date();
                String type = "Message";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                String now = simpleDateFormat.format(date);
                Toast.makeText(ChatDetailActivity.this, now, Toast.LENGTH_SHORT).show();
                String msg = text_send.getText().toString();
                if(!msg.equals("")){
                    sendMessage(uid,friendUid,msg,now,type);
                }
                text_send.setText("");
            }
        });

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = null;
                try{
                    user = dataSnapshot.getValue(User.class);
                }catch (Exception e){
                    Toast.makeText(ChatDetailActivity.this, "Kenaps", Toast.LENGTH_SHORT).show();
                    Log.e("Errorss",e.getMessage());
                }
                readMessages(uid,friendUid);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void sendMessage(String sender,String receiver,String message,String time,String type){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("time",time);
        hashMap.put("type",type);
        reference.child("ChatDetail").push().setValue(hashMap);

    }

    private void readMessages(final String id, final String friendUid){
        chatDetails = new ArrayList<>();
        reference = FirebaseDatabase.getInstance().getReference("ChatDetail");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatDetails.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatDetail chat = snapshot.getValue(ChatDetail.class);
                    if(chat.getReceiver().equals(id) && chat.getSender().equals(friendUid) || chat.getReceiver().equals(friendUid) && chat.getSender().equals(id)){
                        chatDetails.add(chat);
                    }
                    messageAdapter = new MessageAdapter(ChatDetailActivity.this,chatDetails);
                    recyclerView.setAdapter(messageAdapter);
                    pushNotif();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void pushNotif(){
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        String notif_id = "Notif";
        Toast.makeText(ChatDetailActivity.this, "Testing", Toast.LENGTH_SHORT).show();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            @SuppressLint("WrongConstant")
            NotificationChannel notificationChannel = new NotificationChannel(notif_id,"Test Notification", NotificationManager.IMPORTANCE_MAX);
            notificationChannel.setDescription("Chat Notification");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            Toast.makeText(ChatDetailActivity.this, "Notif Masuk", Toast.LENGTH_SHORT).show();

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(ChatDetailActivity.this,notif_id);

        builder.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL).setSmallIcon(R.drawable.adduser).setContentTitle("New Chat").setContentText("Chat");
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(1,builder.build());
    }

}
