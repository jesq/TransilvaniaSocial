package com.example.transilvaniasocial;

public class FindUsersModel {
    String fullName, status, profileImage;

    FindUsersModel () {}

    FindUsersModel(String fullName, String status, String profileImage){
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
