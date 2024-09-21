package com.example.wanderview;

import android.net.Uri;

public class ImageModel {
    private final String imageUrl;
    private final String title;
    private final String author;
    private final Uri userProfileImage;

    public ImageModel(String imageUrl, String title, String author, Uri userProfileImage){
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
        this.userProfileImage = userProfileImage;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {return author; }

    public Uri getUserProfileImage() { return userProfileImage; }
}
