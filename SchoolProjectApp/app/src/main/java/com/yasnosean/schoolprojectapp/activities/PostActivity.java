package com.yasnosean.schoolprojectapp.activities;

import android.content.Context;
import android.content.Intent;
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class PostActivity extends AppCompatActivity {

    private TextView nameDisplay;
    private EditText msgInput;
    private ImageView imageView;

    private boolean uploadImage = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        Toolbar toolbar = findViewById(R.id.addPost_toolbar);
        setSupportActionBar(toolbar);

        SharedPreferences sp = getSharedPreferences("user_info", 0);

        String firstName = sp.getString("first_name", "");
        String lastName = sp.getString("last_name", "");

        getSupportActionBar().setTitle("Create Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameDisplay = findViewById(R.id.post_activity_nameDisplay);
        msgInput = findViewById(R.id.post_activity_input);
        imageView = findViewById(R.id.post_imageView);

        nameDisplay.setText(firstName + " " + lastName);

        uploadImage = getIntent().getExtras().getBoolean("addPhoto");

        if (uploadImage) {
            msgInput.setHint("Say something about this photo...");
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Gallery"), 5);
        }
    }

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

            if (TextUtils.isEmpty(body)) {
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                Toast.makeText(this, "Can't post an empty post", Toast.LENGTH_SHORT).show();
                v.vibrate(250);
            } else {
                intent.putExtra("body", body);
                setResult(RESULT_OK, intent);

                Intent serviceIntent = new Intent(PostActivity.this, PostUploadService.class);
                ContextCompat.startForegroundService(PostActivity.this, serviceIntent);

                finish();
            }
        } else {
            Intent intent = new Intent();
            setResult(RESULT_CANCELED, intent);
            finish();
        }

        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == 5) {
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
