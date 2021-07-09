package com.example.transilvaniasocial;

public class MessagesModel {
    public String date, message, sender, time;

    public MessagesModel() {

    }

    public MessagesModel(String date, String message, String sender, String time) {
        this.date = date;
        this.message = message;
        this.sender = sender;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

}
