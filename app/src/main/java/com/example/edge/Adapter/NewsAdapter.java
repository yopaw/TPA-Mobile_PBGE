package com.example.edge.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.edge.Activity.NewsDetailActivity;
import com.example.edge.Model.News;
import com.example.edge.R;
import com.example.edge.Utils.NewsHelper;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {

    private List<News> news = new ArrayList<>();
    private Context context;

    public NewsAdapter(Context context){
        this.context = context;
    }

    public void setNews(List<News> news) {
        this.news.clear();
        this.news.addAll(news);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.news_item,parent,false);
        return new NewsAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final News currentNews = news.get(position);
//        holder.titleTxt.setText(currentNews.getTitle());
        holder.categoryTxt.setText(currentNews.getCategory());
        holder.authorTxt.setText(currentNews.getAuthor());
        Picasso.with(context).load(currentNews.getUrlToImage()).into(holder.newsImage);
        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NewsHelper.imageUrl = currentNews.getUrlToImage();
                NewsHelper.newsAuthor = currentNews.getAuthor();
                NewsHelper.newsName = currentNews.getTitle();
                Intent intent = new Intent(context, NewsDetailActivity.class);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return news.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView titleTxt,authorTxt,categoryTxt,commentTxt;
        public ImageView newsImage;
        public CardView cardView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
//            titleTxt = itemView.findViewById(R.id.newsTitleTxt);
            authorTxt = itemView.findViewById(R.id.authorsTxt);
            categoryTxt = itemView.findViewById(R.id.newsCategoryTxt);
//            commentTxt = itemView.findViewById(R.id.commentTxt);
            newsImage = itemView.findViewById(R.id.newsPicture);
            cardView = itemView.findViewById(R.id.currentPost);
        }
    }
}
