package com.yasnosean.schoolprojectapp.activities;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.yasnosean.schoolprojectapp.models.Profile;
import com.yasnosean.schoolprojectapp.R;
import com.yasnosean.schoolprojectapp.adapters.SearchProfileAdapter;
import com.yasnosean.schoolprojectapp.models.ServerConnector;
import com.yasnosean.schoolprojectapp.models.ServerHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText search;

    private ListView listView;
    private SearchProfileAdapter searchProfileAdapter;

    private List<Profile> profiles;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        listView = findViewById(R.id.search_listView);
        Toolbar toolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("");

        search = findViewById(R.id.main_toolbar_searchText);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_SEARCH) {
                    String _username = search.getText().toString();

                    Search search = new Search(SearchActivity.this);
                    JSONArray users = search.sendUsernameAndGetUsers(_username);

                    if (users == null) {
                        return false;
                    }

                    profiles = new ArrayList<>();


                    for (int i = 0; i < users.length(); i++) {
                        JSONObject user;
                        try {
                            user = (JSONObject) users.get(i);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            continue;
                        }

                        try {
                            String profileImage = user.getString("profile_image");
                            String backgroundImage = user.getString("background_image");
                            String firstName = user.getString("first_name");
                            String lastName = user.getString("last_name");
                            String username = user.getString("username");

                            profiles.add(new Profile(firstName, lastName, username, profileImage, backgroundImage));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            continue;
                        }
                    }

                    searchProfileAdapter = new SearchProfileAdapter(SearchActivity.this, 0, profiles);
                    listView.setAdapter(searchProfileAdapter);
                    return true;
                }
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Profile profile = profiles.get(i);

                Intent intent = new Intent(SearchActivity.this, ProfileActivity.class);
                intent.putExtra("firstname", profile.getFirstName());
                intent.putExtra("lastname", profile.getLastName());
                intent.putExtra("fullname", (profile.getFirstName() + " " + profile.getLastName()));
                intent.putExtra("username", profile.getUsername());

                startActivity(intent);
            }
        });

    }

    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private class Search extends ServerConnector {

        public Search(Context context) {
            super(context, "search");
        }

        public JSONArray sendUsernameAndGetUsers(String username) {
            serverHandler.sendMessage(username);

            try {
                String usersString;
                while ((usersString = serverHandler.getMessage()).equals("")) {
                }

                JSONArray users = new JSONArray(usersString);
                serverHandler.closeConnection();
                return users;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            serverHandler.closeConnection();
            return null;
        }
    }
}
