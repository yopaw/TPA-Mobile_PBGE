package com.example.edge.Model;

import com.google.firebase.Timestamp;

public class Comment {
    private String user_id;
    private String comment_id;
    private String post_id;
    private String body;
    private Timestamp timestamp;

    public Comment() {

    }

    public Comment(String user_id, String comment_id, String post_id, String body, Timestamp timestamp) {
        this.user_id = user_id;
        this.comment_id = comment_id;
        this.post_id = post_id;
        this.body = body;
        this.timestamp = timestamp;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getComment_id() {
        return comment_id;
    }

    public void setComment_id(String comment_id) {
        this.comment_id = comment_id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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
