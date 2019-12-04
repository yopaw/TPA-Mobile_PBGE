package com.example.edge.Fragment;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.edge.Adapter.ChatAdapter;
import com.example.edge.Adapter.FriendAdapter;
import com.example.edge.Model.ChatHeader;
import com.example.edge.Model.User;
import com.example.edge.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ChatHeaderFragment extends Fragment {

    FirebaseFirestore firestore;

    FirebaseAuth auth;
    FirebaseStorage storage;
    StorageReference reference;
    DatabaseReference dbReference;
    User currentUser;
    private ArrayList<User> friends;
    private RecyclerView recyclerView;
    private ChatAdapter friendAdapter;
    private List<ChatHeader> chatHeaders;
    private Spinner chatSp;
    private ArrayAdapter<String> filterChat;
    private ArrayList<ChatHeader> filterChats;




    public ChatHeaderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_header, container, false);
        List<String> filter = new ArrayList<>();
        filter.add("All");
        filter.add("Favorite");
        filter.add("Archived");
        auth = FirebaseAuth.getInstance();
        filterChat =  new ArrayAdapter<String>(
                getContext(),android.R.layout.simple_spinner_item,filter);
        filterChat.setDropDownViewResource(android.R.layout.simple_spinner_item);
        firestore = FirebaseFirestore.getInstance();
        recyclerView = view.findViewById(R.id.recyclerView);
        chatSp = view.findViewById(R.id.chatSp);
        chatSp.setAdapter(filterChat);
        chatHeaders = new ArrayList<>();
        friends = new ArrayList<>();
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("preference", Context.MODE_PRIVATE);
        final String uid = sharedPreferences.getString("uid","");
        getFriends(uid);
        chatSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(parent.getItemAtPosition(position).equals("Favorite")){
                    getFavoriteChat(uid,"Favorite");
                }
                else if(parent.getItemAtPosition(position).equals("Archived")){
                    getFavoriteChat(uid,"Archived");
                }
                else{
                    getFriends(uid);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    private void getFavoriteChat(String id,String filter){
        filterChats = new ArrayList<>();
        friends.clear();
        for(int i = 0 ; i < chatHeaders.size() ; i++){
            if (filter.equals("Favorite")) {
                if (chatHeaders.get(i).getFavorite() == 1) {
                    filterChats.add(chatHeaders.get(i));
                    if (id.equals(filterChats.get(i).getReceiver())) {
                        firestore.collection("users").document(filterChats.get(i).getSender()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    User user;
                                    user = task.getResult().toObject(User.class);
                                    if(!friends.contains(user))friends.add(user);
                                }
                            }
                        });
                    } else if (id.equals(filterChats.get(i).getSender())) {
                        firestore.collection("users").document(filterChats.get(i).getReceiver()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    User user;
                                    user = task.getResult().toObject(User.class);
                                    if(!friends.contains(user))friends.add(user);
                                }
                            }
                        });
                    }
                }
            }
            else{
                if (chatHeaders.get(i).getArchived() == 1) {
                    filterChats.add(chatHeaders.get(i));
                    if (id.equals(filterChats.get(i).getReceiver())) {
                        firestore.collection("users").document(filterChats.get(i).getSender()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    User user;
                                    user = task.getResult().toObject(User.class);
                                    if(!friends.contains(user))friends.add(user);
                                }
                            }
                        });
                    } else if (id.equals(filterChats.get(i).getSender())) {
                        firestore.collection("users").document(filterChats.get(i).getReceiver()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    User user;
                                    user = task.getResult().toObject(User.class);
                                    if(!friends.contains(user))friends.add(user);
                                }
                            }
                        });
                    }
                }
            }
        }
        friendAdapter.setStart(friends,filterChats);
        friendAdapter.notifyDataSetChanged();
    }

    private void getFriends(final String id){
        friends.clear();
        chatHeaders.clear();
        dbReference = FirebaseDatabase.getInstance().getReference("ChatHeader");
        dbReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    ChatHeader chatHeader = snapshot.getValue(ChatHeader.class);
                    chatHeaders.add(chatHeader);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Toast.makeText(getContext(), "SIZE  "+chatHeaders.size(), Toast.LENGTH_SHORT).show();
        for(int i = 0 ; i < chatHeaders.size() ; i++){
            if(id.equals(chatHeaders.get(i).getReceiver())){
                firestore.collection("users").document(chatHeaders.get(i).getSender()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            User user;
                            user = task.getResult().toObject(User.class);
                            if(!friends.contains(user))friends.add(user);
                            if(friends.size() > 0){
                                Toast.makeText(getContext(), ""+friends.size(), Toast.LENGTH_SHORT).show();
//                                        Toast.makeText(getContext(), "Pasti masuk", Toast.LENGTH_SHORT).show();
                                recyclerView.setHasFixedSize(true);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                friendAdapter = new ChatAdapter(getContext());
                                friendAdapter.setStart(friends,chatHeaders);
                                friendAdapter.notifyDataSetChanged();
                                recyclerView.setAdapter(friendAdapter);
                            }
                        }
                    }
                });
            }
            else if(id.equals(chatHeaders.get(i).getSender())){
                firestore.collection("users").document(chatHeaders.get(i).getReceiver()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            User user;
                            user = task.getResult().toObject(User.class);
                            if(!friends.contains(user))friends.add(user);
                            if(friends.size() > 0){
                                Toast.makeText(getContext(), ""+friends.size(), Toast.LENGTH_SHORT).show();
//                                        Toast.makeText(getContext(), "Pasti masuk", Toast.LENGTH_SHORT).show();
                                recyclerView.setHasFixedSize(true);
                                recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                                friendAdapter = new ChatAdapter(getContext());
                                friendAdapter.setStart(friends,chatHeaders);
                                friendAdapter.notifyDataSetChanged();
                                recyclerView.setAdapter(friendAdapter);
                            }
                        }
                    }
                });
            }
        }
    }
}
