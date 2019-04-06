package com.yasnosean.schoolprojectapp.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yasnosean.schoolprojectapp.helpers.Algorithms;
import com.yasnosean.schoolprojectapp.models.Profile;
import com.yasnosean.schoolprojectapp.R;
import com.yasnosean.schoolprojectapp.models.ProfileManager;
import com.yasnosean.schoolprojectapp.models.ServerHandler;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private Profile profile;

    private ImageView profileImage;
    private ImageView backgroundImage;
    private TextView fullnameText;

    private LinearLayout confirmRejectLayout;
    private Button confirmButton;
    private Button rejectButton;

    private ImageView addRemoveFriendImage;
    private ImageView sendMessageImage;

    private String username;
    private String otherUsername;

    private String userCondition = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.profile_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        profileImage = findViewById(R.id.profile_profile_image);
        backgroundImage = findViewById(R.id.profile_backgroundImage);
        fullnameText = findViewById(R.id.profile_full_name);
        confirmRejectLayout = findViewById(R.id.profile_confirm_reject_layout);
        confirmButton = findViewById(R.id.profile_confirm_request_button);
        rejectButton = findViewById(R.id.profile_reject_request_button);
        addRemoveFriendImage = findViewById(R.id.profile_add_friend);
        sendMessageImage = findViewById(R.id.profile_send_message);

        SharedPreferences sp = getSharedPreferences("user_info", 0);

        username = sp.getString("username", "");

        String firstname = getIntent().getExtras().getString("firstname");
        String lastname = getIntent().getExtras().getString("lastname");
        String fullName = getIntent().getExtras().getString("fullname");
        otherUsername = getIntent().getExtras().getString("username");

//        profile = new Profile("", "", firstname, lastname, otherUsername);
        profile = new Profile(firstname, lastname, username);

        fullnameText.setText(fullName);

        ProfileManager profileManager = new ProfileManager(ProfileActivity.this);
        String profileImageString = profileManager.getProfileImage(otherUsername);

        profileManager = new ProfileManager(ProfileActivity.this);
        String backgroundImageString = profileManager.getBackgroundImage(otherUsername);

        if (!TextUtils.isEmpty(profileImageString)) {
            if (!profileImageString.equals("noimage")) {
                profile.setProfileImage(profileImageString);
                profileImage.setImageBitmap(Algorithms.stringToBitMap(profileImageString));
            } else {

            }
        }
        if (!TextUtils.isEmpty(backgroundImageString)) {
            if (!backgroundImageString.equals("noimage")) {
                profile.setBackgroundImage(backgroundImageString);
                backgroundImage.setImageBitmap(Algorithms.stringToBitMap(backgroundImageString));
            }
        }

        // Checking if the profile that the user is looking at is his profile
        if (otherUsername.equals(username)) {
            handleProfile();
        } else {
            handleOtherProfile();
        }

        addRemoveFriendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FriendProfileManager friendProfileManager = new FriendProfileManager(ProfileActivity.this);
                switch (userCondition) {
                    case "FRIENDS":
                        DialogInterface.OnClickListener diOnClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                switch (i) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        if (friendProfileManager.removeUserAsFriend(username, otherUsername)) {
                                            userCondition = "NOTFRIENDS";
                                            addRemoveFriendImage.setImageResource(R.drawable.add_friend);
                                        } else
                                            Toast.makeText(ProfileActivity.this, "something went wrong", Toast.LENGTH_SHORT).show();
                                        break;
                                }
                            }
                        };

                        AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                        builder.setMessage("Remove this user as a friend?")
                                .setPositiveButton("Yes", diOnClickListener)
                                .setNegativeButton("No", diOnClickListener)
                                .show();
                        break;
                    case "NOTFRIENDS": // That means that the user wants to send a new friend request
                        if (friendProfileManager.sendFriendRequest(username, otherUsername)) {
                            addRemoveFriendImage.setImageResource(R.drawable.cancel_sent_friend_request);
                            userCondition = "SENTREQUEST";
                        } else
                            Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        break;
                    case "SENTREQUEST": // That means that the user wants to cancel the friend request that he had sent
                        if (friendProfileManager.cancelFriendRequest(username, otherUsername)) {
                            addRemoveFriendImage.setImageResource(R.drawable.add_friend);
                            userCondition = "NOTFRIENDS";
                        } else
                            Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendProfileManager friendProfileManager = new FriendProfileManager(ProfileActivity.this);

                if (friendProfileManager.confirmFriendRequest(username, otherUsername)) {
                    userCondition = "FRIENDS";
                    addRemoveFriendImage.setVisibility(View.VISIBLE);
                    addRemoveFriendImage.setImageResource(R.drawable.friends);
                    confirmRejectLayout.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });

        rejectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FriendProfileManager friendProfileManager = new FriendProfileManager(ProfileActivity.this);

                if (friendProfileManager.rejectFriendRequest(username, otherUsername)) {
                    userCondition = "NOTFRIENDS";
                    addRemoveFriendImage.setVisibility(View.VISIBLE);
                    addRemoveFriendImage.setImageResource(R.drawable.add_friend);
                    confirmRejectLayout.setVisibility(View.INVISIBLE);
                } else {
                    Toast.makeText(ProfileActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void handleProfile() {
        addRemoveFriendImage.setVisibility(View.INVISIBLE);
        sendMessageImage.setVisibility(View.INVISIBLE);
        confirmRejectLayout.setVisibility(View.INVISIBLE);

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Gallery"), 420);
            }
        });

        backgroundImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Gallery"), 419);
            }
        });
    }

    private void handleOtherProfile() {
        FriendProfileManager friendProfileManager = new FriendProfileManager(ProfileActivity.this);
        userCondition = friendProfileManager.userCondition(username, otherUsername);


        switch (userCondition) {
            case "FRIENDS":
                confirmRejectLayout.setVisibility(View.INVISIBLE);
                addRemoveFriendImage.setImageResource(R.drawable.friends);
                break;
            case "NOTFRIENDS":
                confirmRejectLayout.setVisibility(View.INVISIBLE);
                addRemoveFriendImage.setImageResource(R.drawable.add_friend);
                break;
            case "SENTREQUEST":
                confirmRejectLayout.setVisibility(View.INVISIBLE);
                addRemoveFriendImage.setImageResource(R.drawable.cancel_sent_friend_request);
                break;
            case "FRIENDREQUEST":
                addRemoveFriendImage.setVisibility(View.INVISIBLE);
                addRemoveFriendImage.setImageBitmap(null);
                break;
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 420) {
                Uri uri = data.getData();
                try {
                    Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    profile.setProfileImage(Algorithms.bitmapToString(bm));

                    ProfileManager profileManager = new ProfileManager(ProfileActivity.this);

                    if (profileManager.setProfileImage(username, profile.getProfileImage())) {
                        profileImage.setImageBitmap(Algorithms.stringToBitMap(profile.getProfileImage()));
                    } else {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }



                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == 419) {
                Uri uri = data.getData();
                try {
                    Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    profile.setBackgroundImage(Algorithms.bitmapToString(bm));

                    ProfileManager profileManager = new ProfileManager(ProfileActivity.this);

                    if (profileManager.setBackgroundImage(username, profile.getBackgroundImage())) {
                        backgroundImage.setImageBitmap(Algorithms.stringToBitMap(profile.getBackgroundImage()));
                    } else {
                        Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
                    }



                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class FriendProfileManager {

        private ServerHandler serverHandler;

        public FriendProfileManager(Context context) {
            serverHandler = new ServerHandler(context);
        }

        private String sendCommand(String command, String username, String otherUsername) {
            serverHandler.start();
            serverHandler.sendMessage(command);
            serverHandler.sendMessage(username);
            serverHandler.sendMessage(otherUsername);

            String answer;

            while ((answer = serverHandler.getMessage()) == "") {
            }

            serverHandler.closeConnection();
            return answer;
        }

        // Checking what is the condition with the profile that the user is looking at
        // There are 4 conditions :
        // - Both users are friends - FRIENDS
        // - Both users are not friends with each other - NOTFRIENDS
        // - The user has already sent a friend request to the other user - SENTREQUEST
        // - The user needs to confirm or reject the friend request that the other user has sent - FRIENDREQUEST
        public String userCondition(String username, String otherUsername) {
            return sendCommand("userCondition", username, otherUsername);
        }

        // Sending a friend request to the other user.
        // If everything is fine, return true. Else, returns false.
        public boolean sendFriendRequest(String username, String otherUsername) {
            return Boolean.valueOf(sendCommand("sendFriendRequest", username, otherUsername));
        }

        // Canceling the friend request that has been sent.
        // If everything is fine, returns true. Else, returns false.
        public boolean cancelFriendRequest(String username, String otherUsername) {
            return Boolean.valueOf(sendCommand("cancelFriendRequest", username, otherUsername));
        }

        // Confirming the friend request that the other user has sent.
        // If everything is fine, returns true. Else, returns false.
        public boolean confirmFriendRequest(String username, String otherUsername) {
            return Boolean.valueOf(sendCommand("confirmFriendRequest", username, otherUsername));
        }

        // Rejecting the friend request that the other user has sent.
        // If everything is fine, returns true. Else, returns false.
        public boolean rejectFriendRequest(String username, String otherUsername) {
            return Boolean.valueOf(sendCommand("rejectFriendRequest", username, otherUsername));
        }

        // Removing the friend as being as the user's friend
        // If everything is fine, returns true. ELse, returns false.
        public boolean removeUserAsFriend(String username, String otherUsername) {
            return Boolean.valueOf(sendCommand("removeUserAsFriend", username, otherUsername));
        }
    }

}
