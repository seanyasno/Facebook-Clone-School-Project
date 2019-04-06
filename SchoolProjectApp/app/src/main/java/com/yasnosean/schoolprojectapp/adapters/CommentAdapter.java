package com.yasnosean.schoolprojectapp.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.yasnosean.schoolprojectapp.R;
import com.yasnosean.schoolprojectapp.activities.ProfileActivity;
import com.yasnosean.schoolprojectapp.helpers.Algorithms;
import com.yasnosean.schoolprojectapp.helpers.ImageLoadTask;
import com.yasnosean.schoolprojectapp.models.Post;
import com.yasnosean.schoolprojectapp.models.Profile;
import com.yasnosean.schoolprojectapp.models.ProfileManager;

import java.util.List;

public class CommentAdapter extends ArrayAdapter<Post> {

    private Context context;
    private List<Post> comments;
    private List<Profile> profiles;

    public CommentAdapter(@NonNull Context context, int resource, int textViewResourceId, List<Post> comments, List<Profile> profiles) {
        super(context, resource, textViewResourceId, comments);

        this.context = context;
        this.comments = comments;
        this.profiles = profiles;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_comment, parent, false);

        CommentHolder holder = new CommentHolder();

        holder.profileImage = view.findViewById(R.id.comments_profile_image);
        holder.body = view.findViewById(R.id.comment_text);
//        Button likeBtn = view.findViewById(R.id.comment_likeBtn);
//        Button replyBtn = view.findViewById(R.id.comment_replyBtn);

        Post temp = comments.get(position);

        new ImageLoadTask(context, holder.profileImage, profiles, position, comments).execute();

        holder.body.setText(temp.getUser() + "\n" + temp.getBody());
        notifyDataSetChanged();

        return view;
    }

    private class CommentHolder {
        private ImageView profileImage;
        private TextView body;
    }
}
