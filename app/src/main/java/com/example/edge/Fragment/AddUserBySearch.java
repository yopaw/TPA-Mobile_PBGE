package com.example.edge.Fragment;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.edge.Model.User;
import com.example.edge.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AddUserBySearch extends Fragment {


    SearchView searchView;
    TextView idTxt,notFoundTxt;
    ImageView profileView;

    FirebaseFirestore firestore;
    FirebaseStorage storage;
    StorageReference reference;
    ArrayList<User> users;
    Button addFriendBtn;
    public AddUserBySearch() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_user_by_search, container, false);

        addFriendBtn = view.findViewById(R.id.addBtn);
        searchView = view.findViewById(R.id.searchBar);
        idTxt = view.findViewById(R.id.idTxt);
        notFoundTxt = view.findViewById(R.id.notFoundTxt);
        profileView = view.findViewById(R.id.profile);



        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String id = searchView.getQuery().toString();
                Toast.makeText(getContext(), ""+id, Toast.LENGTH_SHORT).show();
                firestore = FirebaseFirestore.getInstance();
                try{
                    firestore.collection("users").whereEqualTo("id",id).limit(1).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            users = null;
                            users = (ArrayList<User>) queryDocumentSnapshots.toObjects(User.class);
                            if(!users.isEmpty()) {
                                storage = FirebaseStorage.getInstance();
                                reference = storage.getReference().child("profile"+users.get(0).getUID());
                                reference.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if(task.isSuccessful()){
                                            addFriendBtn.setVisibility(View.VISIBLE);
                                            notFoundTxt.setVisibility(View.INVISIBLE);
                                            Picasso.with(getContext()).load(task.getResult()).into(profileView);
                                            profileView.setVisibility(View.VISIBLE);
                                            idTxt.setText(users.get(0).getId());
                                            idTxt.setVisibility(View.VISIBLE);
                                            addFriendBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    SharedPreferences sharedPreferences = getContext().getSharedPreferences("preference", Context.MODE_PRIVATE);
                                                    final String uid = sharedPreferences.getString("uid","");
                                                    firestore.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if(task.isSuccessful()){
                                                                ArrayList<String> friends = new ArrayList<>();
                                                                DocumentSnapshot documentSnapshot = task.getResult();
                                                                User user = documentSnapshot.toObject(User.class);
                                                                user.setFriendsUid(new ArrayList<String>());
                                                                friends = user.getFriendsUid();
                                                                friends.add(users.get(0).getUID());
                                                                firestore.collection("users").document(uid).update("friendsUid",friends).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        Toast.makeText(getContext(), "Friend Added", Toast.LENGTH_SHORT).show();
                                                                    }
                                                                });
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                });
                            }
                            else{
                                addFriendBtn.setVisibility(View.INVISIBLE);
                                profileView.setVisibility(View.INVISIBLE);
                                idTxt.setVisibility(View.INVISIBLE);
                                notFoundTxt.setVisibility(View.VISIBLE);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "Gagal", Toast.LENGTH_SHORT).show();
                        }
                    });
                }catch (Exception e){
                    Toast.makeText(getContext(), "Gagal", Toast.LENGTH_SHORT).show();
                }

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return view;
    }

}
