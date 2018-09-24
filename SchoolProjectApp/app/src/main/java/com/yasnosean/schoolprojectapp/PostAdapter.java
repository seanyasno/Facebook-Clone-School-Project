package com.yasnosean.schoolprojectapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class PostAdapter extends ArrayAdapter<Post> {

    private Context context;
    private List<Post> posts;

    private boolean likeChanged = false;
    private Post lastPostChanged = null;

    public PostAdapter(Context context, int resouce, int textViewResouceId, List<Post> posts) {
        super(context, resouce, textViewResouceId, posts);

        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_post, parent, false);

        TextView user = view.findViewById(R.id.post_user);
        TextView body = view.findViewById(R.id.post_body);
        TextView likes = view.findViewById(R.id.post_likes);
        TextView comments = view.findViewById(R.id.post_comments);

        final Button likeBtn = view.findViewById(R.id.post_likeBtn);

        final Post temp = posts.get(position);

        if (temp.isLiked()) {
            likeBtn.setTextColor(Color.parseColor("#FF3F4FB5")); //FF3F4FB5
        }

        user.setText(temp.getUser());
        body.setText(temp.getBody());
        likes.setText(String.valueOf(temp.getLikes()) + " Likes");
        comments.setText(String.valueOf(temp.getComments().size()) + " Comments");

        likeBtn.setOnClickListener(new View.OnClickListener() {
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

        return view;
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

    public List<Post> getPosts() {
        return posts;
    }
}
