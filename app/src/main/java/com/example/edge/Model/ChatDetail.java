package com.example.edge.Model;

public class ChatDetail {
    private String sender;
    private String receiver;
    private String message;
    private String time;
    private String type;

    public ChatDetail(String sender, String receiver, String message, String time, String type) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.time = time;
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ChatDetail(){

    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
