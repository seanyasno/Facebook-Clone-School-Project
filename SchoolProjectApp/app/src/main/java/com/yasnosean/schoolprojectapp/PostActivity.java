package com.yasnosean.schoolprojectapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class PostActivity extends AppCompatActivity {

    private EditText msgInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        msgInput = findViewById(R.id.post_activity_input);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_post, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        if (item.getItemId() == R.id.menu_post_postBtn) {
            String body = msgInput.getText().toString();
            Intent intent = new Intent();

            if (TextUtils.isEmpty(body)) {
                setResult(RESULT_CANCELED, intent);
            } else {
                intent.putExtra("body", body);
                setResult(RESULT_OK, intent);
            }
            finish();
        }

        return true;
    }
}
