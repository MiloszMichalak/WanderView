package com.example.wanderview.PostModel;

public class ImageModel {
    private final String imageUrl;
    private final String title;
    private final String author;
    private final String userProfileImage;
    private final String uid;
    private final String key;
    long timestamp;
    long likes;
    boolean isUserLiked;
    long commentAmount;
    boolean isTemp;

    public ImageModel(String imageUrl, String title, String author, String userProfileImage, String uid, String key,
                      long timestamp, long likes, boolean isUserLiked, long commentAmount){
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
        this.userProfileImage = userProfileImage;
        this.uid = uid;
        this.key = key;
        this.timestamp = timestamp;
        this.likes = likes;
        this.isUserLiked = isUserLiked;
        this.commentAmount = commentAmount;
    }

    public ImageModel(String imageUrl, String title, String author, String userProfileImage, String uid, String key,
                      long timestamp, long likes, boolean isUserLiked, long commentAmount, boolean isTemp){
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
        this.userProfileImage = userProfileImage;
        this.uid = uid;
        this.key = key;
        this.timestamp = timestamp;
        this.likes = likes;
        this.isUserLiked = isUserLiked;
        this.commentAmount = commentAmount;
        this.isTemp = isTemp;
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

    public long getLikes() { return likes; }

    public long getCommentAmount() { return commentAmount; }

    public boolean isTemp() { return isTemp; }

    public void setLikes(long likes) { this.likes = likes; }
}
