package com.yasnosean.schoolprojectapp;

import java.util.ArrayList;

public class Post {

    private String id;
    private String user;
    private String body;
    private int likes = 0;
    private ArrayList<Post> comments;
    private boolean liked = false;

    public Post(String user, String body) {
        this.user = user;
        this.body = body;
        this.id = String.valueOf(System.currentTimeMillis());
        comments = new ArrayList<>();
    }

    public Post(String id, String user, String body) {
        this.user = user;
        this.body = body;
        this.id = id;
        comments = new ArrayList<>();
    }

    public Post(String id, String user, String body, int likes) {
        this.id = id;
        this.user = user;
        this.body = body;
        this.likes = likes;
        comments = new ArrayList<>();
    }

    public Post(String id, String user, String body, int likes, boolean liked) {
        this.id = id;
        this.user = user;
        this.body = body;
        this.likes = likes;
        this.liked = liked;
        comments = new ArrayList<>();
    }

    public String getUser() {
        return user;
    }

    public String getBody() {
        return body;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public ArrayList<Post> getComments() {
        return comments;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void addComment(Post comment) {
        comments.add(comment);
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getId() {
        return id;
    }
}
