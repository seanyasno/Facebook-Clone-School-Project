package com.yasnosean.schoolprojectapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrConfig;
import com.r0adkll.slidr.model.SlidrPosition;
import com.yasnosean.schoolprojectapp.adapters.CommentAdapter;
import com.yasnosean.schoolprojectapp.models.Post;
import com.yasnosean.schoolprojectapp.models.Profile;
import com.yasnosean.schoolprojectapp.R;
import com.yasnosean.schoolprojectapp.models.ServerConnector;
import com.yasnosean.schoolprojectapp.models.ServerHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class CommentsActivity extends AppCompatActivity implements View.OnClickListener{

    private ListView listView;
    private ArrayList<Post> comments;
    private CommentAdapter commentAdapter;
    private EditText commentInput;
    private Button addCommentBtn;

    private String username = "";

    private List<Profile> profiles;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        SlidrConfig config = new SlidrConfig.Builder()
                .position(SlidrPosition.TOP)
                .build();
        Slidr.attach(this, config);

        SharedPreferences sp = getSharedPreferences("user_info", 0);
        username = sp.getString("username", "");

        commentInput = findViewById(R.id.comments_commentInput);
        addCommentBtn = findViewById(R.id.comments_addCommentBtn);
        addCommentBtn.setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        loadComments();
    }

    protected void onDestroy() {
        super.onDestroy();

        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    private void loadComments() {
        this.comments = new ArrayList<>();
        this.profiles = new LinkedList<>();

        Intent intent = getIntent();
        String postId = intent.getStringExtra("post_id");

        CommentsLoader commentsLoader = new CommentsLoader(this, postId);
        JSONArray comments = commentsLoader.getComments();

        if (comments == null) {
            System.out.println("BATMAN SOMETHING COOL");
            return;
        }
        try {
            for (int i = 0; i < comments.length(); i++) {
                String c = comments.getString(i);
                JSONObject jComment = new JSONObject(c);

                String id = jComment.getString("upload_time");
                String user = jComment.getString("user");
                String username = jComment.getString("username");
                String body = jComment.getString("body");
                String image = jComment.getString("image");
                int likes = jComment.getInt("likes");

                boolean usernameInList = false;
                for (Profile p : profiles) {
                    if (p.getUsername().equals(username)) {
                        usernameInList = true;
                        break;
                    }
                }
                if (!usernameInList) {
                    Profile profile = new Profile("", "", username);
                    profiles.add(profile);
                }

                this.comments.add(new Post(id, user, username, body, image, likes, false, comments));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        Collections.reverse(this.comments);
        commentAdapter = new CommentAdapter(this, 0, 0, this.comments, this.profiles);
        listView = findViewById(R.id.comments_listView);
        listView.setAdapter(commentAdapter);
    }

    public void onClick(View view) {
        if (view == addCommentBtn) {
            SharedPreferences sp = getSharedPreferences("user_info", 0);
            String firstName = sp.getString("first_name", "");
            String lastName = sp.getString("last_name", "");
            String fullName = firstName + " " + lastName;

            String body = commentInput.getText().toString();

            JSONObject post = new JSONObject();
            try {
                post.put("upload_time", String.valueOf(System.currentTimeMillis()));
                post.put("user", fullName);
                post.put("username", username);
                post.put("body", body);
                post.put("image", "");
                post.put("likes", 0);
                post.put("users", new JSONArray());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Intent intent = getIntent();
            String postId = intent.getStringExtra("post_id");

            // Adds a new comment to the server's database
            ServerHandler serverHandler = new ServerHandler(this);
            serverHandler.start();
            serverHandler.sendMessage("addcomment");
            serverHandler.sendMessage(postId);
            serverHandler.sendMessage(username);
            serverHandler.sendMessage(post.toString());

            // Reloads the activity to see the new comment;
            finish();
            startActivity(getIntent());
        }
    }

    private class CommentsLoader extends ServerConnector {

        public CommentsLoader(Context context, String postId) {
            super(context, "commentsloader");
            serverHandler.sendMessage(postId);
        }

        public JSONArray getComments() {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String comments = serverHandler.getMessage();
            System.out.println("IRONMAN " + comments);
            try {
                JSONArray jComments = new JSONArray(comments);
                return jComments;
            } catch (JSONException e) {
                System.out.println("IRONMAN " + e.toString());
            }
            return null;
        }

    }

}