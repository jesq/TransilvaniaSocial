package com.example.transilvaniasocial;

public class InboxModel {
    String uid, id2;

    public InboxModel(){
    }

    public InboxModel(String uid, String id2) {
        this.uid = uid;
        this.id2 = id2;
    }

    public String getId2() {
        return id2;
    }

    public void setId2(String id2) {
        this.id2 = id2;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
