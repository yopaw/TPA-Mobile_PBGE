package com.example.edge.Activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.edge.Adapter.NewsAdapter;
import com.example.edge.Model.News;
import com.example.edge.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class NewsActivity extends AppCompatActivity {

    private RequestQueue queue;
    private ArrayList<News> news;

    private Spinner spinner;
    private RecyclerView recyclerView;
    private NewsAdapter newsAdapter;

    private ArrayList<News> filterNews;
    private ArrayList<String> sources;
    private ArrayAdapter<CharSequence> adapter;
    private ArrayList<String> filters;

    ArrayAdapter<String> spinnerArrayAdapter;
    List<String> newsList;
    String[] newz = new String[]{

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news);
        queue = Volley.newRequestQueue(this);
        recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setHasFixedSize(true);
        spinner = findViewById(R.id.filter);
        newsList = new ArrayList<>(Arrays.asList(newz));
        spinnerArrayAdapter = new ArrayAdapter<String>(
                this,android.R.layout.simple_spinner_item,newsList);
        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_item);
        spinner.setAdapter(spinnerArrayAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        news = new ArrayList<>();
        sources = new ArrayList<>();
        newsAdapter = new NewsAdapter(NewsActivity.this);
        getNews();
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                if(parent.getItemAtPosition(position).toString().equals("Test")){
//                    filterNews("Test");
//                }
//                else if(parent.getItemAtPosition(position).toString().equals("technology")){
//                    filterNews("technology");
//                }
//                else if(parent.getItemAtPosition(position).toString().equals("general")){
//                    filterNews("general");
//                }
//                else if(parent.getItemAtPosition(position).toString().equals("default")){
//                    getNews();
//                    getSource();
//                }
                filterNews(parent.getItemAtPosition(position).toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void filterNews(String category){
        filterNews = new ArrayList<>();
        for(int i = 0 ; i < news.size() ; i++){
            if(news.get(i).getCategory().equals(category)){
                filterNews.add(news.get(i));
            }
        }
        newsAdapter.setNews(filterNews);
        newsAdapter.notifyDataSetChanged();
    }

    private void getSource(){
        String url = "https://newsapi.org/v2/sources?apiKey=18fbb399eff3485bac84458365cce088";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    sources.clear();
                    newsList.clear();
                    JSONArray jsonArray = response.getJSONArray("sources");
                    for (int i = 0 ; i < jsonArray.length() ; i++){
                        JSONObject source = jsonArray.getJSONObject(i);
//                        if(!sources.contains(source.getString("name")))sources.add(source.getString("name"));
                        for(int j = 0 ; j < news.size() ; j++){
                            if(news.get(j).getSoruce().equals(source.getString("name"))){
                                news.get(j).setCategory(source.getString("category"));
                                if(!newsList.contains(source.getString("category"))){
//                            Toast.makeText(NewsActivity.this, ""+source.getString("category"), Toast.LENGTH_SHORT).show();
                                    newsList.add(source.getString("category"));
                                }
                            }
                        }

                    }
                    newsList.add("default");
                    newsList.add("Test");
                    for(int i = 0 ; i < news.size() ; i++){
                        if(news.get(i).getCategory() == null){
                            news.get(i).setCategory("default");
                        }
                    }
                    spinnerArrayAdapter.notifyDataSetChanged();
                    recyclerView.setAdapter(newsAdapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(request);
    }

    private void getNews() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String selectedDate = sdf.format(Calendar.getInstance().getTime());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(Calendar.getInstance().getTime());
        calendar.add(Calendar.DATE,-1);
        String yesterdayDate = sdf.format(calendar.getTime());
        Toast.makeText(this, ""+yesterdayDate, Toast.LENGTH_SHORT).show();
        String url = "https://newsapi.org/v2/everything?q=bitcoin&from="+yesterdayDate+"&sortBy=publishedAt&apiKey=18fbb399eff3485bac84458365cce088";
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray = response.getJSONArray("articles");
                    for (int i = 0 ; i < jsonArray.length() ; i++){
                        JSONObject article = jsonArray.getJSONObject(i);
                        News currentNews = new News();
                        currentNews.setAuthor(null);
                        currentNews.setAuthor(article.getString("author"));
                        currentNews.setTitle(article.getString("title"));
                        currentNews.setUrlToImage(article.getString("urlToImage"));
                        currentNews.setUrl(article.getString("url"));
                        currentNews.setPublishedAt(article.getString("publishedAt"));
                        currentNews.setContent(article.getString("content"));
                        currentNews.setSoruce(article.getJSONObject("source").getString("name"));
                        if(currentNews.getAuthor() != null && currentNews.getAuthor().length() > 6) {
                            currentNews.setAuthor(currentNews.getAuthor().substring(0,5));
                        }
                        if(!currentNews.getAuthor().equals("null") )news.add(currentNews);
                    }
                    newsAdapter.setNews(news);
                    newsAdapter.notifyDataSetChanged();
                    getSource();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
        queue.add(request);
    }
}
