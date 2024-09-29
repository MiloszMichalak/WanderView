package com.example.wanderview;

public class ImageModel {
    private final String imageUrl;
    private final String title;
    private final String author;
    private final String userProfileImage;
    private String uid;

    public ImageModel(String imageUrl, String title, String author, String userProfileImage){
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
        this.userProfileImage = userProfileImage;
    }

    public ImageModel(String imageUrl, String title, String author, String userProfileImage, String uid){
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
        this.userProfileImage = userProfileImage;
        this.uid = uid;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {return author; }

    public String getUserProfileImage() { return userProfileImage; }

    public String getUid() { return uid; }
}
