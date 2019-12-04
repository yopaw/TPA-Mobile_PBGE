package com.example.edge.Activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.edge.Model.User;
import com.example.edge.R;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    EditText emailTxt,passwordTxt;
    Button loginBtn;

    SignInButton signInButton;

    GoogleSignInClient mGoogleSignInClient;
    FirebaseAuth auth;
    String currUid;
    private int RC_SIGN_IN = 0;

    private CallbackManager callbackManager;
    private static final String EMAIL = "email";
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        auth = FirebaseAuth.getInstance();
        emailTxt = findViewById(R.id.emailTxt);
        passwordTxt = findViewById(R.id.passwordTxt);
        loginBtn = findViewById(R.id.loginBtn);
        signInButton = findViewById(R.id.sign_in_button);

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTxt.getText().toString();
                String password = passwordTxt.getText().toString();
                loginAuth(email,password);
            }
        });

        callbackManager = CallbackManager.Factory.create();

        loginButton = findViewById(R.id.facebookLoginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(LoginActivity.this, Arrays.asList(EMAIL));
                LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            // App code
                            Log.d("fb", "msk");
                            handleFacebookAccessToken(loginResult.getAccessToken());
                        }

                        @Override
                        public void onCancel() {
                            // App code
                        }

                        @Override
                        public void onError(FacebookException exception) {
                            // App code
                        }
                    });
            }
        });
    }

    private void loginAuth(String email,String password){
        auth.signInWithEmailAndPassword(email,password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                String uid = auth.getCurrentUser().getUid();
                Toast.makeText(LoginActivity.this, "Login Success", Toast.LENGTH_SHORT).show();
                Toast.makeText(LoginActivity.this, uid , Toast.LENGTH_SHORT).show();
                final SharedPreferences preferences = getSharedPreferences("preference",MODE_PRIVATE);
                final SharedPreferences.Editor editor = preferences.edit();
                editor.putString("uid",uid);

                FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                firestore.collection("users").document(uid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            DocumentSnapshot documentSnapshot = task.getResult();
                            User user;
                            user = documentSnapshot.toObject(User.class);
                            editor.putInt("state",user.getState());
                            editor.apply();
                            if(user.getState() == 1){
                                Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                startActivity(intent);
                            }
                            else{
                                Intent intent = new Intent(LoginActivity.this,FinishRegister.class);
                                startActivity(intent);
                            }
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(LoginActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(LoginActivity.this);
//        FirebaseUser currentUser = auth.getCurrentUser();
//        if (currentUser != null) {
//            updateUI();
//        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void getUserByEmail(){

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
            Toast.makeText(this, ""+account.getEmail(), Toast.LENGTH_SHORT).show();
            firestore.collection("users").whereEqualTo("email",account.getEmail()).limit(1).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        ArrayList<User> users = (ArrayList) task.getResult().toObjects(User.class);
                        currUid = users.get(0).getUID();
                        SharedPreferences sharedPreferences = getSharedPreferences("preference",MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("uid",currUid);
                        editor.apply();
                        String uid = sharedPreferences.getString("uid","");
                        Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                        startActivity(intent);
                    }
                }
            });
//            String email = emailTxt.getText().toString();
//            String password = passwordTxt.getText().toString();
            // Signed in successfully, show authenticated UI.
//            loginAuth(email,password);


        } catch (ApiException e) {
            Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Toast.makeText(this, ""+e.getStatusCode(), Toast.LENGTH_SHORT).show();
            Log.w("Haha", "signInResult:failed code=" + e.getStatusCode());
            Toast.makeText(LoginActivity.this, "Failed Login With Google", Toast.LENGTH_SHORT).show();
//            updateUI(null);
        }
    }

    private void updateUI(GoogleSignInAccount account) {

    }

    private void handleFacebookAccessToken(AccessToken token) {
        final String TAG = "FACEBOOK LOG";
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            final FirebaseUser user = auth.getCurrentUser();
                            SharedPreferences sharedPreferences = getSharedPreferences("preference",MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("uid",auth.getCurrentUser().getUid());
                            editor.apply();

                            FirebaseFirestore firestore = FirebaseFirestore.getInstance();
                            firestore.collection("users").document(user.getUid()).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                @Override
                                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                                    if (documentSnapshot.exists()) {
                                        Intent intent = new Intent(LoginActivity.this,HomeActivity.class);
                                        startActivity(intent);
                                    }
                                    else {
                                        Map<String, Object> map = new HashMap<>();
                                        map.put("uid", user.getUid());
                                        map.put("state", 1);
                                        map.put("email", user.getEmail());
                                        map.put("friendsUid", null);
                                        map.put("password", null);
                                        map.put("phoneNumber", null);
                                        map.put("url", null);
                                        map.put("gender", null);
                                        Intent intent = new Intent(LoginActivity.this,FinishRegister.class);
                                        startActivity(intent);
                                    }
                                }
                            });
//                            Toast.makeText(LoginActivity.this, ""+auth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
//                            updateUI();
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI();
                        }
                    }
                });
    }

    private void updateUI() {

        Toast.makeText(LoginActivity.this, "You're logged in", Toast.LENGTH_LONG).show();
//        SharedPreferences sharedPreferences = getSharedPreferences()
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }
}
