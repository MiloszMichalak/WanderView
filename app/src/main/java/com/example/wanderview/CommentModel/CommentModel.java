package com.example.wanderview.CommentModel;

public class CommentModel {
    private final String profileImageUrl;
    private final String author;
    private final long seconds;
    private final String text;
    private long likes;
    public boolean isUserLiked;
    String uid;
    String key;
    String postId;
    String authorPostId;

    public CommentModel(String profileImageUrl, String author, long seconds, String text, long likes,
                        boolean isUserLiked, String uid, String key, String postId, String authorPostId) {
        this.profileImageUrl = profileImageUrl;
        this.author = author;
        this.seconds = seconds;
        this.text = text;
        this.likes = likes;
        this.isUserLiked = isUserLiked;
        this.uid = uid;
        this.key = key;
        this.postId = postId;
        this.authorPostId = authorPostId;
    }

    public String getProfileImageUrl() { return profileImageUrl; }

    public boolean isUserLiked() { return isUserLiked; }

    public long getLikes() { return likes; }

    public String getText() { return text; }

    public long getSeconds() { return seconds; }

    public String getAuthor() { return author; }

    public String getUid() { return uid; }

    public String getKey() { return key; }

    public String getPostId() { return postId; }

    public String getAuthorPostId() { return authorPostId; }

    public void setLikes(long likes) { this.likes = likes; }
}
