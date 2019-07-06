package com.yasnosean.schoolprojectapp.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.widget.ImageView;

import com.yasnosean.schoolprojectapp.R;
import com.yasnosean.schoolprojectapp.models.Post;
import com.yasnosean.schoolprojectapp.models.Profile;
import com.yasnosean.schoolprojectapp.models.ProfileManager;

import java.util.List;

public class ImageLoadTask extends AsyncTask<Void, Void, Bitmap> {

    private Context context;
    private ImageView profileImage;
    private List<Profile> profiles;
    private int position;
    private List<Post> list;

    public ImageLoadTask(Context context, ImageView profileImage, List<Profile> profiles, int position, List<Post> list) {
        this.context = context;
        this.profileImage = profileImage;
        this.profiles = profiles;
        this.position = position;
        this.list = list;
    }

    @Override
    protected Bitmap doInBackground(Void... voids) {
        for (Profile profile : profiles) {
            if (profile.getUsername().equals(list.get(position).getUsername())) {
                if (!TextUtils.isEmpty(profile.getProfileImage())) {
                    if (!profile.getProfileImage().equals("noimage")) {
                        return Algorithms.stringToBitMap(profile.getProfileImage());
                    }
                } else {
                    ProfileManager profileManager = new ProfileManager(context);
                    String imageString = profileManager.getProfileImage(list.get(position).getUsername());
                    profile.setProfileImage(imageString);
                    if (!imageString.equals("noimage"))
                        return Algorithms.stringToBitMap(imageString);
                }
                break;
            }
        }

        return BitmapFactory.decodeResource(context.getResources(), R.drawable.profile_default);
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);

        if (profileImage != null) {
            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap);
            }
        }
    }
}
