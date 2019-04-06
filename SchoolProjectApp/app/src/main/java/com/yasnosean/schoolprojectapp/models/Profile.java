package com.yasnosean.schoolprojectapp.models;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

public class Profile extends User {

    private String profileImage;
    private String backgroundImage;

    public Profile(String firstName, String lastName, String username) {
        super(firstName, lastName, username);

        profileImage = "";
        backgroundImage = "";
    }

    public Profile(String firstName, String lastName, String username, String profileImage, String backgroundImage) {
        super(firstName, lastName, username);

        this.profileImage = profileImage;
        this.backgroundImage = backgroundImage;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
