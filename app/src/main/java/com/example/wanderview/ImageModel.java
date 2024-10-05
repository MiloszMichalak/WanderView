package com.example.wanderview;

public class ImageModel {
    private final String imageUrl;
    private final String title;
    private final String author;
    private final String userProfileImage;
    private final String uid;
    private final String key;
    long timestamp;
    int likes;
    boolean isUserLiked;

    public ImageModel(String imageUrl, String title, String author, String userProfileImage, String uid, String key, long timestamp, int likes, boolean isUserLiked){
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
        this.userProfileImage = userProfileImage;
        this.uid = uid;
        this.key = key;
        this.timestamp = timestamp;
        this.likes = likes;
        this.isUserLiked = isUserLiked;
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

    public int getLikes() { return likes; }

    public boolean isUserLiked() { return isUserLiked; }

    public void setLikes(int likes) { this.likes = likes; }
}
