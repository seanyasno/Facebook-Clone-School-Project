package com.yasnosean.schoolprojectapp.adapters;

import com.yasnosean.schoolprojectapp.R;
import com.yasnosean.schoolprojectapp.activities.*;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.yasnosean.schoolprojectapp.activities.ProfileActivity;
import com.yasnosean.schoolprojectapp.helpers.Algorithms;
import com.yasnosean.schoolprojectapp.helpers.ImageLoadTask;
import com.yasnosean.schoolprojectapp.models.Post;
import com.yasnosean.schoolprojectapp.models.Profile;
import com.yasnosean.schoolprojectapp.models.ProfileManager;

import java.util.List;

public class PostAdapter extends ArrayAdapter<Post> {

    private Context context;
    public List<Post> posts;
    private List<Profile> profiles;

    private boolean likeChanged = false;
    private boolean comment = false;
    private Post lastPostChanged = null;

    public PostAdapter(Context context, int resource, int textViewResourceId, List<Post> posts, List<Profile> profiles) {
        super(context, resource, textViewResourceId, posts);

        this.context = context;
        this.posts = posts;
        this.profiles = profiles;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
        @SuppressLint("ViewHolder") View view = layoutInflater.inflate(R.layout.custom_post, parent, false);

        PostHolder postHolder = new PostHolder();

        postHolder.user = view.findViewById(R.id.post_user);
        postHolder.profileImage = view.findViewById(R.id.profile_image);
        postHolder.body = view.findViewById(R.id.post_body);
        postHolder.image = view.findViewById(R.id.post_image);
        postHolder.likes = view.findViewById(R.id.post_likes);
        postHolder.comments = view.findViewById(R.id.post_comments);
        postHolder.likeView = view.findViewById(R.id.post_likeView);
        postHolder.likeIcon = view.findViewById(R.id.post_likeIcon);

        RelativeLayout commentBtn = view.findViewById(R.id.post_commentBtn);
        RelativeLayout likeBtn1 = view.findViewById(R.id.post_likeBtn);

        final Post temp = posts.get(position);

        if (temp.isLiked()) {
            postHolder.likeView.setTextColor(Color.parseColor("#FF3F4FB5")); //FF3F4FB5
            postHolder.likeIcon.setImageResource(R.drawable.blue_like_icon);
        }

        if (!temp.getImage().equals("")) {
            postHolder.image.setImageBitmap(Algorithms.stringToBitMap(temp.getImage()));
        }

        new ImageLoadTask(context, postHolder.profileImage, profiles, position, posts).execute();

        postHolder.user.setText(temp.getUser());
        postHolder.body.setText(temp.getBody());
        postHolder.likes.setText(String.valueOf(temp.getLikes()) + " Likes");
        postHolder.comments.setText(String.valueOf(temp.getComments().length()) + " Comments");

        likeBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!temp.isLiked()) { // like
                    temp.setLiked(true);
                } else { // dislike
                    temp.setLiked(false);
                }
                likeChanged = true;
                lastPostChanged = temp;
                notifyDataSetChanged();
            }
        });

        commentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                comment = true;
                lastPostChanged = temp;
                notifyDataSetChanged();
            }
        });

        return view;
    }

    public boolean isComment() {
        return comment;
    }

    public void setComment(boolean comment) {
        this.comment = comment;
        notifyDataSetChanged();
    }

    public boolean isLikeChanged() {
        return likeChanged;
    }

    public void setLikeChanged(boolean likeChanged) {
        this.likeChanged = likeChanged;
    }

    public Post getLastPostChanged() {
        return lastPostChanged;
    }

    public void setLastPostChanged(Post lastPostChanged) {
        this.lastPostChanged = lastPostChanged;
    }

//    public List<Post> getPosts() {
//        return posts;
//    }

    static class PostHolder {
        TextView user;
        ImageView profileImage;
        TextView body;
        ImageView image;
        TextView likes;
        TextView comments;
        TextView likeView;
        ImageView likeIcon;
    }
}

