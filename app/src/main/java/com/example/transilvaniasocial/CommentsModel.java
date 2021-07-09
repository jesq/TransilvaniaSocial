package com.example.transilvaniasocial;

public class CommentsModel {
    public String commentText, date, time, uid;

    public CommentsModel(){

    }

    public CommentsModel(String commentText, String date, String time, String uid) {
        this.commentText = commentText;
        this.date = date;
        this.time = time;
        this.uid = uid;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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
