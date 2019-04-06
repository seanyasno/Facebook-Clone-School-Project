package com.yasnosean.schoolprojectapp.models;

import android.content.Context;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ServerHandler {

    private OkHttpClient client;
    private WebSocket ws;
    private WebSocketListener listener;

    private Context context;

    private String message = "";

    public ServerHandler(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        client = new OkHttpClient();
        listener = new SocketListener();
    }

    public void start() {
        Request request = new Request.Builder().url("ws://192.168.1.16:8080").build();
//        Request request = new Request.Builder().url("ws://77.138.210.48:80").build();
//        Request request = new Request.Builder().url("ws://172.19.5.82:8080").build();
        ws = client.newWebSocket(request, listener);
    }

    public void sendMessage(String message) {
        ws.send(message);
    }

    public String getMessage() {
        System.out.println("BATMAN GET " + message);
        String msg = message;
        message = "";
        return msg;
    }

    public void resetMessage() { message = ""; }

    public void closeConnection() {
        client.dispatcher().executorService().shutdown();
    }

    private final class SocketListener extends WebSocketListener {

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            message = text;
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(1000, null);
        }
    }
}