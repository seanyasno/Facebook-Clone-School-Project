package com.yasnosean.schoolprojectapp.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.yasnosean.schoolprojectapp.helpers.BitmapHelper;
import com.yasnosean.schoolprojectapp.R;
import com.yasnosean.schoolprojectapp.services.PostUploadService;
import com.yasnosean.schoolprojectapp.services.VibrateReceiver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This activity handles the post upload.
 * The user can upload posts without nor without images here.
 */
public class PostActivity extends AppCompatActivity {

    // UI
    private TextView nameDisplay;
    private EditText msgInput;
    private ImageView imageView;

    // a variable that checks if the user want to make a post with an image.
    private boolean uploadImage = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Toolbar toolbar = findViewById(R.id.addPost_toolbar);
        setSupportActionBar(toolbar);

        // getting user's full name from shared preferences
        SharedPreferences sp = getSharedPreferences("user_info", 0);
        String firstName = sp.getString("first_name", "");
        String lastName = sp.getString("last_name", "");

        getSupportActionBar().setTitle("Create Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // init
        nameDisplay = findViewById(R.id.post_activity_nameDisplay);
        msgInput = findViewById(R.id.post_activity_input);
        imageView = findViewById(R.id.post_imageView);

        // display user's name
        nameDisplay.setText(firstName + " " + lastName);

        uploadImage = getIntent().getExtras().getBoolean("addPhoto");

        /* if the user chooses to upload a post with an image,
            then in this section the gallery opens.
         */
        if (uploadImage) {
            msgInput.setHint("Say something about this photo...");
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Gallery"), 5);
        }
    }

    // showing the post menu button
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.menu_post_postBtn) {
            String body = msgInput.getText().toString();
            Intent intent = new Intent();

            /** a user can't upload empty posts (a post with empty bodies),
             * then if the body is empty, the phone vibrates and a toast is
             * being showed up to notify the user that the body is empty.
             */
            if (TextUtils.isEmpty(body)) {
                Toast.makeText(this, "Can't post an empty post", Toast.LENGTH_SHORT).show();
                VibrateReceiver vibrateReceiver = new VibrateReceiver();
                IntentFilter intentFilter = new IntentFilter("com.yasnosean.schoolprojectapp.android.action.broadcast");
                registerReceiver(vibrateReceiver, intentFilter);

                Intent intent1 = new Intent("com.yasnosean.schoolprojectapp.android.action.broadcast");
                Bundle extras = new Bundle();
                extras.putString("send_data", "test");
                intent1.putExtras(extras);
                sendBroadcast(intent1);
            } else {
                /** Body isn't empty so the post can be uploaded. A foreground service is being called
                 * to make sure that the posts is 100 percent being uploaded.
                 * After the upload is finished, the activity is closed.
                 */
                intent.putExtra("body", body);
                setResult(RESULT_OK, intent);

                Intent serviceIntent = new Intent(PostActivity.this, PostUploadService.class);
                ContextCompat.startForegroundService(PostActivity.this, serviceIntent);

                finish();
            }
        } else {
            /** The user clicked on the back button so he goes back the MainActivity. */
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }

        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // a code section that checks if the user chose a photo from his gallery.
        if (resultCode == RESULT_OK) {
            if (requestCode == 5) {
                // getting the image's data
                Uri uri = data.getData();
                imageView.setImageURI(uri);

                try {
                    Bitmap bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    imageView.setImageBitmap(bm);

                    BitmapHelper.getInstance().setBitmap(bm);

                    SharedPreferences sp = getSharedPreferences("imageInstance", 0);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("image", true);
                    editor.commit();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
