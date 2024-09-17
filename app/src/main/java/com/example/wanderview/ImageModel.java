package com.example.wanderview;

public class ImageModel {
    private final String imageUrl;
    private final String title;
    private final String author;

    public ImageModel(String imageUrl, String title, String author){
        this.imageUrl = imageUrl;
        this.title = title;
        this.author = author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {return author; }
}
