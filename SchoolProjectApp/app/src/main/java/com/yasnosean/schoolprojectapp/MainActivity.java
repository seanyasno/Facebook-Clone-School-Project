package com.yasnosean.schoolprojectapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    private ListView listView;
    private Button addPostBtn;
    private SwipeRefreshLayout swipeRefreshLayout;

    private ArrayList<Post> posts;
    private PostAdapter postAdapter;

    private String fullName = "";
    private String username = "";
    private boolean refresh = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences sp = getSharedPreferences("user_info", 0);

        String firstName = sp.getString("first_name", "");
        String lastName = sp.getString("last_name", "");
        fullName = firstName + " " + lastName;
        username = sp.getString("username", "");

        addPostBtn = findViewById(R.id.main_addPostBtn);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);

        loadPosts();

        addPostBtn.setOnClickListener(addPostClick);
        listView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (postAdapter.isLikeChanged()) {
                    postAdapter.setLikeChanged(false);
                    PostManager postManager = new PostManager(MainActivity.this, "like");
                    postManager.sendMessage(username);

                    Post post = postAdapter.getLastPostChanged();

                    if (post.isLiked())
                        postManager.sendMessage("yes");
                    else
                        postManager.sendMessage("no");

                    JSONObject jPost = new JSONObject();

                    try {
                        jPost.put("id", post.getId());
                        jPost.put("user", post.getUser());
                        jPost.put("body", post.getBody());
                        jPost.put("likes", post.getLikes());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    postManager.sendPost(jPost);

                    int likes = postManager.getLikes();
                    if (likes != -1) {
                        // finds the index of the post and sets the current likes
                        int index = postAdapter.getPosts().indexOf(post);
                        postAdapter.getPosts().get(index).setLikes(likes);

                        // updates manually the amount of likes from here
                        View v = listView.getChildAt(index);
                        TextView likesText = v.findViewById(R.id.post_likes);
                        likesText.setText(String.valueOf(likes) + " Likes");
                    } else {
                        Toast.makeText(MainActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPosts();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_red_light,android.R.color.holo_green_light,android.R.color.holo_purple,android.R.color.holo_blue_light);

    }

    View.OnClickListener addPostClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            startActivityForResult(new Intent(MainActivity.this, PostActivity.class), 1);
        }
    };

    private void loadPosts() {
        this.posts = new ArrayList<>();

        PostLoader postLoader = new PostLoader(MainActivity.this);
        JSONArray posts = postLoader.getPosts();

        if (posts == null) {
            System.out.println("BATMAN SOMETHING COOL");
            return;
        }
        try {
            for (int i = 0; i < posts.length(); i++) {
                String p = posts.getString(i);
                JSONObject jPost = new JSONObject(p);

                String id = jPost.getString("id");
                String user = jPost.getString("user");
                String body = jPost.getString("body");
                int likes = jPost.getInt("likes");
                JSONArray s = jPost.getJSONArray("users");
                boolean liked = false;

                for (int j = 0; j < s.length(); j++) {
                    if (s.getString(j).equals(username)) {
                        liked = true;
                        break;
                    }
                }

                this.posts.add(new Post(id, user, body, likes, liked));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Collections.reverse(this.posts);
        postAdapter = new PostAdapter(this, 0, 0, this.posts);
        listView = findViewById(R.id.main_listView);
        listView.setAdapter(postAdapter);
        swipeRefreshLayout.setRefreshing(false);
        refresh = false;
    }

    private class PostLoader {

        private ServerHandler serverHandler;
        private Context context;

        public PostLoader(Context context) {
            this.context = context;
            serverHandler = new ServerHandler(context, WhichSocketListener.POSTLOADER);
            serverHandler.start();
            serverHandler.sendMessage("postloader");
        }

        public JSONArray getPosts() {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String posts = serverHandler.getMessager();

            try {
                JSONObject jsonObject = new JSONObject(posts);
                JSONArray jPosts = jsonObject.getJSONArray("posts");
                return jPosts;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                String body = data.getExtras().getString("body");

                PostUploader postUploader = new PostUploader(MainActivity.this);
                postUploader.sendPost(body);

                Toast.makeText(this, "post uploaded", Toast.LENGTH_SHORT).show();

                finish();
                startActivity(getIntent());
            }
        }
    }

    private class PostManager implements IServerHandler {

        private ServerHandler serverHandler;
        private JSONObject post;

        private Context context;

        public PostManager(Context context, String state) {
            this.context = context;
            serverHandler = new ServerHandler(context, WhichSocketListener.POSTMANAGER);
            serverHandler.start();
            serverHandler.sendMessage("post" + state);
        }

        public int getLikes() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String s = serverHandler.getMessager();

            if (s.equals(""))
                return -1;
            return Integer.valueOf(serverHandler.getMessager());
        }

        public void sendMessage(String message) {
            serverHandler.sendMessage(message);
        }

        public void sendPost(JSONObject post) {
            this.post = post;
            serverHandler.sendMessage(post.toString());
        }

        public void sendUser(JSONObject user) {

        }

        public JSONObject getUserByUsername(String username) {
            return null;
        }
    }

    private class PostUploader {

        private ServerHandler serverHandler;
        private Context context;

        public PostUploader(Context context) {
            this.context = context;
            serverHandler = new ServerHandler(context, WhichSocketListener.POSTUPLOADER);
            serverHandler.start();
            serverHandler.sendMessage("postuploader");
        }

        public void sendPost(String body) {
            JSONObject post = new JSONObject();

            try {
                post.put("id", String.valueOf(System.currentTimeMillis()));
                post.put("user", fullName);
                post.put("body", body);
                post.put("likes", 0);
                post.put("users", new JSONArray(new Object[] {}));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            serverHandler.sendMessage(post.toString());
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_messenger, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        return true;
    }
}
