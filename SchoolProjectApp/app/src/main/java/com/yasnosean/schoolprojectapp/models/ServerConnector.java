package com.yasnosean.schoolprojectapp.models;

import android.content.Context;

public class ServerConnector {

    protected ServerHandler serverHandler;
    protected Context context;

    public ServerConnector(Context context, String command) {
        this.context = context;
        serverHandler = new ServerHandler(context);
        serverHandler.start();
        serverHandler.sendMessage(command);
    }

}
