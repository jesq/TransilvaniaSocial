package com.example.transilvaniasocial.model;

public class PostsModel {
    public String date;
    public String description;
    public String postImage;
    public String time;
    public String uid;

    public PostsModel() {}

    public PostsModel(String date, String description, String postImage, String time, String uid){
        this.date = date;
        this.description = description;
        this.postImage = postImage;
        this.time = time;
        this.uid = uid;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
