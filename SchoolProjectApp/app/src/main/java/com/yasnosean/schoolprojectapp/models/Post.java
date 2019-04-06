package com.yasnosean.schoolprojectapp.models;

import org.json.JSONArray;

import java.util.ArrayList;

public class Post {

    private String id;
    private String user;
    private String username;
    private String body;
    private String image;
    private int likes = 0;
    private JSONArray comments;
    private boolean liked = false;

    public Post(String user, String body) {
        this.user = user;
        this.body = body;
        this.id = String.valueOf(System.currentTimeMillis());
        comments = new JSONArray();
    }

    public Post(String id, String user, String body) {
        this.user = user;
        this.body = body;
        this.id = id;
        comments = new JSONArray();
    }

    public Post(String id, String user, String body, int likes) {
        this.id = id;
        this.user = user;
        this.body = body;
        this.likes = likes;
        comments = new JSONArray();
    }

    public Post(String id, String user, String body, String image, int likes, boolean liked) {
        this.id = id;
        this.user = user;
        this.body = body;
        this.image = image;
        this.likes = likes;
        this.liked = liked;
        comments = new JSONArray();
    }

    public Post(String id, String user, String username, String body, String image, int likes, boolean liked, JSONArray comments) {
        this.id = id;
        this.user = user;
        this.username = username;
        this.body = body;
        this.image = image;
        this.likes = likes;
        this.liked = liked;
        this.comments = comments;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    public JSONArray getComments() {
        return comments;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
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
