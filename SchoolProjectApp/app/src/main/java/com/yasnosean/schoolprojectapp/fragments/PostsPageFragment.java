package com.yasnosean.schoolprojectapp.fragments;


import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yasnosean.schoolprojectapp.R;
import com.yasnosean.schoolprojectapp.activities.CommentsActivity;
import com.yasnosean.schoolprojectapp.activities.PostActivity;
import com.yasnosean.schoolprojectapp.activities.StartActivity;
import com.yasnosean.schoolprojectapp.adapters.PostAdapter;
import com.yasnosean.schoolprojectapp.helpers.Algorithms;
import com.yasnosean.schoolprojectapp.helpers.BitmapHelper;
import com.yasnosean.schoolprojectapp.models.Post;
import com.yasnosean.schoolprojectapp.models.Profile;
import com.yasnosean.schoolprojectapp.models.ProfileManager;
import com.yasnosean.schoolprojectapp.models.ServerConnector;
import com.yasnosean.schoolprojectapp.services.PostUploadService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class PostsPageFragment extends Fragment implements View.OnClickListener {

    private ImageView postsProfileImage;
    private ListView listView;
    private Button addPostBtn;
    private ImageView addImageBtn;
    private SwipeRefreshLayout swipeRefreshLayout;

    private List<Post> posts;
    private PostAdapter postAdapter;

    private List<Profile> profiles;

    private String fullName = "";
    private static String username = "";

    private View v;

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_posts_page, container, false);

        init();
        loadPosts(v);

        addPostBtn.setOnClickListener(this);
        addImageBtn.setOnClickListener(this);

        listView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
                if (postAdapter.isLikeChanged()) {
                    postAdapter.setLikeChanged(false);
                    PostManager postManager = new PostManager(getActivity(), "like");
                    postManager.sendMessage(username);

                    Post post = postAdapter.getLastPostChanged();

                    if (post.isLiked()) {
                        postManager.sendMessage("yes");
                    } else
                        postManager.sendMessage("no");

                    JSONObject jPost = new JSONObject();

                    try {
                        jPost.put("upload_time", post.getId());
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
                        int index = postAdapter.posts.indexOf(post);
                        System.out.println("MARVEL " + String.valueOf(index));
                        postAdapter.posts.get(index).setLikes(likes);

                        // updates manually the amount of likes from here
                        System.out.println("MARVEL SIZE" + listView.getChildCount());
                        View v = listView.getChildAt(index);
                        if (v != null) {
                            Toast.makeText(getContext(), "LIKES: " + likes, Toast.LENGTH_SHORT).show();
                            TextView likesText = v.findViewById(R.id.post_likes);
                            likesText.setText(String.valueOf(likes) + " Likes");
                            postAdapter.notifyDataSetChanged();
                        } else {
                            System.out.println("MARVEL SOMETHING WENT WRONG");
                        }
                    } else {
                        Toast.makeText(getActivity(), "something went wrong with the like", Toast.LENGTH_SHORT).show();
                    }
                }

                if (postAdapter.isComment()) {
                    postAdapter.setComment(false);
                    Intent intent = new Intent(getActivity(), CommentsActivity.class);
                    intent.putExtra("post_id", postAdapter.getLastPostChanged().getId());
                    startActivity(intent);
                }
            }
        });

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                posts = new ArrayList<>();
                profiles = new LinkedList<>();

                PostLoader postLoader = new PostLoader(getActivity());
                JSONArray jsonPosts = postLoader.getPosts();

                listView = v.findViewById(R.id.main_listView);

                PostLoadTask postLoadTask = new PostLoadTask(getContext(), postsProfileImage, username, swipeRefreshLayout);

                postLoadTask.execute(jsonPosts);

                if (postLoadTask.getStatus() == AsyncTask.Status.FINISHED) {
                    Toast.makeText(getContext(), "done", Toast.LENGTH_SHORT).show();
                }
            }
        });

        swipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_dark);




        return v;
    }

    private void init() {
        SharedPreferences sp = getActivity().getSharedPreferences("user_info", 0);

        String firstName = sp.getString("first_name", "");
        String lastName = sp.getString("last_name", "");
        fullName = firstName + " " + lastName;
        username = sp.getString("username", "");

        postsProfileImage = v.findViewById(R.id.posts_profile_image);
        addPostBtn = v.findViewById(R.id.main_addPostBtn);
        addImageBtn = v.findViewById(R.id.main_addImagePostBtn);
        swipeRefreshLayout = v.findViewById(R.id.swipe_refresh_layout);

        ProfileManager profileManager = new ProfileManager(getContext());
        String img = profileManager.getProfileImage(username);

        if (!img.equals("noimage")) {
            postsProfileImage.setImageBitmap(Algorithms.stringToBitMap(img));
        }
    }

    private void loadPosts(View v) {
        this.posts = new ArrayList<>();
        profiles = new LinkedList<>();

        PostLoader postLoader = new PostLoader(getActivity());
        JSONArray posts = postLoader.getPosts();

        if (posts == null)
            return;

        try {
            for (int i = 0; i < posts.length(); i++) {
                JSONArray subPosts = new JSONArray(posts.get(i).toString());

                for (int j = 0; j < subPosts.length(); j++) {
                    JSONObject jPost = subPosts.getJSONObject(j);

                    String upload_time = jPost.getString("upload_time");
                    String user = jPost.getString("user");
                    String username = jPost.getString("username");
                    String body = jPost.getString("body");
                    String image = jPost.getString("image");
                    int likes = jPost.getInt("likes");
                    JSONArray s = jPost.getJSONArray("users");
                    JSONArray comments = jPost.getJSONArray("comments");
                    boolean liked = false;

                    boolean usernameInList = false;
                    for (Profile p : profiles) {
                        if (p.getUsername().equals(username)) {
                            usernameInList = true;
                            break;
                        }
                    }
                    if (!usernameInList) {
                        Profile profile = new Profile(user.split("")[0], user.split("")[1], username);
                        profiles.add(profile);

                        if (username.equals(this.username)) {
                            ProfileManager profileManager = new ProfileManager(getContext());
                            postsProfileImage.setImageBitmap(Algorithms.stringToBitMap(profileManager.getProfileImage(username)));
                        }
                    }

                    for (int k = 0; k < s.length(); k++) {
                        if (s.getString(k).equals(username)) {
                            liked = true;
                            break;
                        }
                    }



                    this.posts.add(new Post(upload_time, user, username, body, image, likes, liked, comments));
                }

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Collections.reverse(this.posts);
        postAdapter = new PostAdapter(getActivity(), 0, 0, this.posts, profiles);
        listView = v.findViewById(R.id.main_listView);
        listView.setAdapter(postAdapter);
        swipeRefreshLayout.setRefreshing(false);
    }

    public void onClick(View view) {
        if (view == addImageBtn || view == addPostBtn) {
            Intent intent = new Intent(getActivity(), PostActivity.class);
            if (view == addImageBtn)
                intent.putExtra("addPhoto", true);
            else
                intent.putExtra("addPhoto", false);
            startActivityForResult(intent, 1);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == -1) { // -1 = RESULT_OK
            if (requestCode == 1) {

                String body = data.getExtras().getString("body");

                SharedPreferences sp = getActivity().getSharedPreferences("imageInstance", 0);
                Bitmap bitmap = null;
                String imgString = "";
                if (sp.getBoolean("image", false))
                    bitmap = BitmapHelper.getInstance().getBitmap();

                if (bitmap != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                    byte[] decodeString = baos.toByteArray();

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;

                    BitmapFactory.decodeByteArray(decodeString, 0, decodeString.length, options);
                    int imageHeight = options.outHeight;
                    int imageWidth = options.outWidth;
                    int inSampleSize = 1;

                    if (imageHeight > imageWidth) {
                        if (imageHeight > 800 && imageWidth > 480) {
                            final int halfHeight = imageHeight / 1;
                            final int halfWidth = imageWidth / 1;

                            while ((halfHeight / inSampleSize) > 800 || (halfWidth / inSampleSize) > 480) {
                                inSampleSize *= 2;
                            }
                        }
                    } else {
                        if (imageHeight > 480 && imageWidth > 800) {
                            final int halfHeight = imageHeight / 1;
                            final int halfWidth = imageWidth / 1;

                            while ((halfHeight / inSampleSize) > 800 || (halfWidth / inSampleSize) > 480) {
                                inSampleSize *= 2;
                            }
                        }
                    }

                    options.inSampleSize = inSampleSize;
                    options.inJustDecodeBounds = false;

                    Bitmap decoded = BitmapFactory.decodeByteArray(decodeString, 0, decodeString.length, options);
                    System.out.println("SUPERMAN " + String.valueOf(decoded.getWidth()) + "x" + String.valueOf(decoded.getHeight()));
                    imgString = Base64.encodeToString(decodeString, Base64.DEFAULT);
                }

                PostUploader postUploader = new PostUploader(getActivity());
                String answer = postUploader.sendPost(body, imgString);

                if (answer.equals("done")) {
                    Toast.makeText(getActivity(), "post uploaded", Toast.LENGTH_SHORT).show();

                    getActivity().stopService(new Intent(getActivity(), PostUploadService.class));
                    showNotification(getActivity(), "Post Upload", "The post has been uploaded.", new Intent(getActivity(), StartActivity.class));

                    getActivity().finish();
                    startActivity(getActivity().getIntent());
                }


            }
        }
    }

    public void showNotification(Context context, String title, String body, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        int notificationId = 1;
        String channelId = "channel-01";
        String channelName = "Channel Name";
        int importance = NotificationManager.IMPORTANCE_HIGH;

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_android)
                .setContentTitle(title)
                .setContentText(body);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(intent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(resultPendingIntent);

        notificationManager.notify(notificationId, mBuilder.build());
    }

    public static class PostLoader extends ServerConnector {

        public PostLoader(Context context) {
            super(context, "postloader");
            serverHandler.sendMessage(username);
        }

        // TODO: work on getPosts()
        public JSONArray getPosts() {
            String numOfPosts;
            while ((numOfPosts = serverHandler.getMessage()) == "") {
            }
            System.out.println("NICEE " + numOfPosts);

            String posts = "[[";
            try {
                if (Integer.valueOf(numOfPosts) > 0) {
                    String post;
                    while ((post = serverHandler.getMessage()) == "") {
                    }
                    serverHandler.sendMessage("ok");
                    posts += post;
                    for (int i = 1; i < Integer.valueOf(numOfPosts); i++) {
                        while ((post = serverHandler.getMessage()) == "") {
                        }
                        serverHandler.sendMessage("ok");
                        posts += "," + post;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(context, "There was a problem, please try again", Toast.LENGTH_SHORT).show();
            }
            posts += "]]";

            System.out.println("VERYNICE\n" + posts);
            try {
                return new JSONArray(posts);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

    }

    private class PostManager extends ServerConnector {

        public PostManager(Context context, String state) {
            super(context, "post" + state);
        }

        // TODO: check it
        public int getLikes() {
            String s = "";
            while ((s = serverHandler.getMessage()).equals("")) {}

            if (s.equals(""))
                return -1;

            Toast.makeText(context, s, Toast.LENGTH_SHORT).show();

            return Integer.valueOf(s);
        }

        public void sendMessage(String message) {
            serverHandler.sendMessage(message);
        }

        public void sendPost(JSONObject post) {
            serverHandler.sendMessage(post.toString());
        }

    }

    private class PostUploader extends ServerConnector {

        public PostUploader(Context context) {
            super(context, "postuploader");
        }

        public String sendPost(String body, String img) {
            JSONObject post = new JSONObject();

            try {
                post.put("upload_time", String.valueOf(System.currentTimeMillis()));
                post.put("user", fullName);
                post.put("username", username);
                post.put("body", body);
                if (TextUtils.isEmpty(img))
                    post.put("image", "");
                else
                    post.put("image", "users/" + username + "/" + String.valueOf(System.currentTimeMillis()) + ".jpg");

                post.put("likes", 0);
                post.put("users", new JSONArray(new Object[] {}));
                post.put("comments", new JSONArray(new Object[] {}));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            serverHandler.sendMessage(post.toString());
            serverHandler.sendMessage(username);
            serverHandler.sendMessage(img);

            String ans = "";
            while ((ans = serverHandler.getMessage()).equals("")){}
            return ans;
        }

    }

    class PostLoadTask extends AsyncTask<JSONArray, Void, List<Post>> {

        private Context context;
        private ImageView postProfileImage;
        private String username;

//        private List<Post> posts;
//        private List<Profile> profiles;

//        private PostAdapter postAdapter;
//        private ListView listView;
        private SwipeRefreshLayout swipeRefreshLayout;

        private Bitmap postProfileImageBitmap;

        public PostLoadTask(Context context, ImageView postProfileImage, String username, SwipeRefreshLayout swipeRefreshLayout) {
            this.context = context;
            this.postProfileImage = postProfileImage;
            this.username = username;
            this.swipeRefreshLayout = swipeRefreshLayout;
        }

        @Override
        protected List<Post> doInBackground(JSONArray... jsonObjects) {
            JSONArray jsonPosts = jsonObjects[0];

            if (jsonPosts == null)
                return null;

            try {
                for (int i = 0; i < jsonPosts.length(); i++) {
                    JSONArray subPosts = new JSONArray(jsonPosts.get(i).toString());

                    for (int j = 0; j < subPosts.length(); j++) {
                        JSONObject jPost = subPosts.getJSONObject(j);

                        String upload_time = jPost.getString("upload_time");
                        String user = jPost.getString("user");
                        String username = jPost.getString("username");
                        String body = jPost.getString("body");
                        String image = jPost.getString("image");
                        int likes = jPost.getInt("likes");
                        JSONArray s = jPost.getJSONArray("users");
                        JSONArray comments = jPost.getJSONArray("comments");
                        boolean liked = false;

                        boolean usernameInList = false;
                        for (Profile p : profiles) {
                            if (p.getUsername().equals(username)) {
                                usernameInList = true;
                                break;
                            }
                        }
                        if (!usernameInList) {
                            Profile profile = new Profile(user.split("")[0], user.split("")[1], username);
                            profiles.add(profile);

                            if (username.equals(this.username)) {
                                ProfileManager profileManager = new ProfileManager(context);
                                postProfileImageBitmap = Algorithms.stringToBitMap(profileManager.getProfileImage(username));
                            }
                        }

                        for (int k = 0; k < s.length(); k++) {
                            if (s.getString(k).equals(username)) {
                                liked = true;
                                break;
                            }
                        }



                        posts.add(new Post(upload_time, user, username, body, image, likes, liked, comments));
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return posts;
        }


        @Override
        protected void onPostExecute(List<Post> list) {
            super.onPostExecute(list);

            List<Post> postsList = list;
            Collections.reverse(postsList);
            posts = postsList;
            postAdapter = new PostAdapter(context, 0, 0, posts, profiles);
            listView.setAdapter(postAdapter);
            swipeRefreshLayout.setRefreshing(false);
            postProfileImage.setImageBitmap(postProfileImageBitmap);
        }
    }

}
