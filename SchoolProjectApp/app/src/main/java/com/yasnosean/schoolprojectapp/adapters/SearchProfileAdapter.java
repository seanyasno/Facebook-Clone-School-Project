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
import android.widget.ImageView;
import android.widget.TextView;

import com.yasnosean.schoolprojectapp.R;
import com.yasnosean.schoolprojectapp.activities.ProfileActivity;
import com.yasnosean.schoolprojectapp.helpers.Algorithms;
import com.yasnosean.schoolprojectapp.models.Profile;
import com.yasnosean.schoolprojectapp.models.ProfileManager;

import java.util.List;

public class SearchProfileAdapter extends ArrayAdapter<Profile> {

    private Context context;
    private List<Profile> profiles;

    public SearchProfileAdapter(@NonNull Context context, int resource, List<Profile> profiles) {
        super(context, resource, profiles);

        this.context = context;
        this.profiles = profiles;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = ((Activity)context).getLayoutInflater();
        View view = layoutInflater.inflate(R.layout.custom_search_profile, parent, false);

        ImageView profileImage = view.findViewById(R.id.custom_search_profile_image);
        TextView fullname = view.findViewById(R.id.custom_search_full_name);

        Profile profile = profiles.get(position);

        ProfileManager profileManager = new ProfileManager(context);
        String profileImageString = profileManager.getProfileImage(profile.getUsername());

        if (!profileImageString.equals("noimage")) {
            profile.setProfileImage(profileImageString);
        }

        if (!TextUtils.isEmpty(profile.getProfileImage())) {
            profileImage.setImageBitmap(Algorithms.stringToBitMap(profile.getProfileImage()));
        }

        fullname.setText(profile.getFirstName() + " " + profile.getLastName());
        return view;
    }
}