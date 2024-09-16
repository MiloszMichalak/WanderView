package com.example.wanderview;

public class ImageModel {
    private final String imageUrl;
    private final String title;

    public ImageModel(String imageUrl, String title){
        this.imageUrl = imageUrl;
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }
}
