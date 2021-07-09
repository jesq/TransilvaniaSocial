package com.example.transilvaniasocial.model;

public class SearchStudentsModel {
    String fullName, status, profileImage;

    SearchStudentsModel() {}

    SearchStudentsModel(String fullName, String status, String profileImage){
        this.fullName = fullName;
        this.status = status;
        this.profileImage = profileImage;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }
}
