package com.example.edge.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.edge.Model.News;
import com.example.edge.R;
import com.example.edge.Utils.NewsHelper;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class NewsDetailActivity extends AppCompatActivity {

    RecyclerView commentView;
    ImageView postImage;
    TextView authorTxt;
    EditText sendText;
    ImageButton btnSend;
    String postID;
    FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);
        commentView = findViewById(R.id.commentView);
        postImage = findViewById(R.id.newsImage);
        authorTxt = findViewById(R.id.authorsTxt);
        sendText = findViewById(R.id.sendText);
        btnSend = findViewById(R.id.btn_send);
        authorTxt.setText(NewsHelper.newsAuthor);
        Picasso.with(this).load(NewsHelper.imageUrl).into(postImage);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }




}
