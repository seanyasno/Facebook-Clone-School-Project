package com.yasnosean.schoolprojectapp;

import org.json.JSONObject;

public interface IServerHandler {

    public void sendUser(JSONObject user);
    public JSONObject getUserByUsername(String username);

}
