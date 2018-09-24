package com.yasnosean.schoolprojectapp;

import android.content.Context;

import java.net.ServerSocket;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

public class ServerHandler {

    private OkHttpClient client;
    private WebSocket ws;
    private WebSocketListener listener;

    private Context context;
    private WhichSocketListener wsl;

    private String messager = "";

    public ServerHandler(Context context, WhichSocketListener wsl) {
        this.context = context;
        this.wsl = wsl;
        init();
    }

    private void init() {
        client = new OkHttpClient();
        switch (wsl) {
            case LOGIN:
                listener = new LoginSocketListener();
                break;
            case REGISTER:
                listener = new RegisterSocketListener();
            case POSTMANAGER:
                listener = new PostManagerSocketListener();
            case POSTLOADER:
                listener = new PostLoaderSocketListener();
            case POSTUPLOADER:
                listener = new PostUploaderSocketListener();
        }
    }

    public void start() {
        Request request = new Request.Builder().url("ws://192.168.1.12:8080").build();
        ws = client.newWebSocket(request, listener);
    }

    public void sendMessage(String message) {
        ws.send(message);
    }

    public String getMessager() {
        System.out.println("BATMAN GET");
        return messager;
    }

    public void closeConnection() {
        client.dispatcher().executorService().shutdown();
    }

    public void setMessager(String messager) {
        this.messager = messager;
    }

    private final class LoginSocketListener extends WebSocketListener {

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            messager = text;
            System.out.println("BATMAN " + text);
        }

        @Override
        public void onClosing(WebSocket webSocket, int code, String reason) {
            webSocket.close(1000, null);
        }
    }

    private final class RegisterSocketListener extends WebSocketListener {

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            messager = text;
            System.out.println("BATMAN GET " + text);
        }
    }

    private final class PostManagerSocketListener extends WebSocketListener {
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            messager = text;
            System.out.println("BATMAN GET " + text);
        }
    }

    private final class PostLoaderSocketListener extends WebSocketListener {
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            messager = text;
            System.out.println("BATMAN GET " + text);
        }
    }

    private final class PostUploaderSocketListener extends WebSocketListener {
        @Override
        public void onMessage(WebSocket webSocket, String text) {
            messager = text;
            System.out.println("BATMAN GET " + text);
        }
    }
}