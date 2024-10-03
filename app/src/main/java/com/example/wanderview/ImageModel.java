package com.example.wanderview;

public class ImageModel {
    private final String imageUrl;
    private final String title;
    private final String author;
    private final String userProfileImage;
    private String uid;
    private String key;
    long timestamp;

    public ImageModel(String imageUrl, String title, String author, String userProfileImage, String key,long timestamp){
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
        this.userProfileImage = userProfileImage;
        this.key = key;
        this.timestamp = timestamp;
    }

    public ImageModel(String imageUrl, String title, String author, String userProfileImage, String uid, String key, long timestamp){
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
        this.userProfileImage = userProfileImage;
        this.uid = uid;
        this.key = key;
        this.timestamp = timestamp;
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

    public long getTimestamp() { return timestamp; }

    public String getKey() { return key; }
}
