package com.example.wanderview;

public class UserModel {
    private final String author;
    private final String username;
    private final String profilePhotoUrl;

    public UserModel(String author, String username, String profilePhotoUrl) {
        this.author = author;
        this.username = username;
        this.profilePhotoUrl = profilePhotoUrl;
    }

    public String getAuthor() { return author; }

    public String getUsername() { return username; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
}
