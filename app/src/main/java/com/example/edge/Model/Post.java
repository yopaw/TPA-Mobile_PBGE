package com.example.edge.Model;

import com.google.firebase.Timestamp;

public class Post {

    private String user_id, desc;
    private Timestamp timestamp;
    private String post_id;

    public Post() {

    }

    public Post(String user_id, String desc, String post_id, Timestamp timestamp) {
        this.user_id = user_id;
        this.desc = desc;
        this.post_id = post_id;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getPost_id() {
        return post_id;
    }

    public void setPost_id(String post_id) {
        this.post_id = post_id;
    }
}
