package com.yasnosean.schoolprojectapp.models;

import android.content.Context;

public class    ProfileManager {
    private ServerHandler serverHandler;

    public ProfileManager(Context context) {
        serverHandler = new ServerHandler(context);
    }

    private String getImage(String command, String username) {
        serverHandler.start();
        serverHandler.sendMessage(command);
        serverHandler.sendMessage(username);

        String answer;

        while ((answer = serverHandler.getMessage()) == "") {
        }

        serverHandler.closeConnection();
        return answer;
    }

    private String setImage(String command, String username, String image) {
        serverHandler.start();
        serverHandler.sendMessage(command);
        serverHandler.sendMessage(username);
        serverHandler.sendMessage(image);

        String answer;

        while ((answer = serverHandler.getMessage()) == "") {
        }

        serverHandler.closeConnection();
        return answer;
    }

    public String getProfileImage(String username) {
        return getImage("getProfileImage", username);
    }

    public String getBackgroundImage(String username) {
        return getImage("getBackgroundImage", username);
    }

    public boolean setProfileImage(String username, String image) {
        return Boolean.valueOf(setImage("changeProfileImage", username, image));
    }

    public boolean setBackgroundImage(String username, String image) {
        return Boolean.valueOf(setImage("changeBackgroundImage", username, image));
    }

}
