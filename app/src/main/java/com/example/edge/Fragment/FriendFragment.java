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
import android.widget.Adapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.edge.Adapter.FriendAdapter;
import com.example.edge.Model.User;
import com.example.edge.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FriendFragment extends Fragment {

    FirebaseFirestore firestore;
    TextView usernameTxt;

    FirebaseAuth auth;
    ImageView profileImage;
    FirebaseStorage storage;
    StorageReference reference;
    User currentUser = new User();
    private ArrayList<User> friends;
    private RecyclerView recyclerView;
    private FriendAdapter friendAdapter;

    public FriendFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friend, container, false);

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        usernameTxt = view.findViewById(R.id.usernameTxt);
        profileImage = view.findViewById(R.id.profile);
        recyclerView = view.findViewById(R.id.recyclerView);
        friends = new ArrayList<>();
        SharedPreferences sharedPreferences = this.getActivity().getSharedPreferences("preference", Context.MODE_PRIVATE);
        String uid = sharedPreferences.getString("uid","");
        storage = FirebaseStorage.getInstance();
        reference = storage.getReference().child("profile"+uid);
        reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if(task.isSuccessful()){
                    Picasso.with(getContext()).load(task.getResult()).into(profileImage);
                }
            }
        });
        firestore.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
//                    Map<String, Object> user = new HashMap<>();HashMap
                    DocumentSnapshot documentSnapshot = task.getResult();
                    currentUser =  documentSnapshot.toObject(User.class);
                    usernameTxt.setText(currentUser.getUsername());
                    getFriends();
                }
                else{
                    Toast.makeText(getContext(), "Gagal", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void getFriends(){
        for(int i = 0 ; i < currentUser.getFriendsUid().size() ; i++){
            firestore.collection("users").document(currentUser.getFriendsUid().get(i)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        User user;
                        DocumentSnapshot documentSnapshot = task.getResult();
                        user = documentSnapshot.toObject(User.class);
                        friends.add(user);
                        if(friends.size() == currentUser.getFriendsUid().size()){
                            friendAdapter = new FriendAdapter(getContext(),friends);
                            recyclerView.setHasFixedSize(true);
                            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                            recyclerView.setAdapter(friendAdapter);
                        }
                    }
                    else{
                        Toast.makeText(getContext(), "Gagal Bro", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

}
