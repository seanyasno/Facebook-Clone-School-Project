package com.yasnosean.schoolprojectapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.yasnosean.schoolprojectapp.R;
import com.yasnosean.schoolprojectapp.models.ServerConnector;
import com.yasnosean.schoolprojectapp.models.ServerHandler;
import com.yasnosean.schoolprojectapp.models.TestService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// This activity is the first activity that the user sees when he launches the app.
// In this activity the user can login and register to the app.
public class StartActivity extends AppCompatActivity implements View.OnClickListener{

    private Button loginBtn;
    private Button registerBtn;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        loginBtn.setOnClickListener(this);
        registerBtn.setOnClickListener(this);
    }

    /** When the user clicks on the login button, it shows a custom popup that lets
     *  the user to login into the app. */
    private void loginOnClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        View v = getLayoutInflater().inflate(R.layout.activity_login, null);

        builder.setView(v);
        final AlertDialog dialog = builder.create();
        dialog.show();

        final EditText usernameInput = v.findViewById(R.id.login_username);
        final EditText passwordInput = v.findViewById(R.id.login_password);
        Button loginBtn = v.findViewById(R.id.login_loginBtn);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login login = new Login(StartActivity.this);

                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();

                JSONObject user = new JSONObject();

                try {
                    user.put("username", username);
                    user.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // send the user data to the server to get response
                // if it is correct or incorrect
                login.sendUser(user);

                // if the user can login, it will load up the next activity
                // if not so it will make a toast
                if (!login.canLogin()) {
                    Toast.makeText(StartActivity.this, "username or password is incorrect", Toast.LENGTH_SHORT).show();
                } else {
                    String token = FirebaseInstanceId.getInstance().getToken();

                    ServerHandler serverHandler = new ServerHandler(StartActivity.this);
                    serverHandler.start();
                    serverHandler.sendMessage("add_registration_id");
                    serverHandler.sendMessage(username);
                    serverHandler.sendMessage(token);
                    serverHandler.closeConnection();

                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    finish();
                    dialog.dismiss();
                }
            }
        });
    }

    /** When the user clicks on the register button, it shows a custom popup that lets
     * the user to register and then login into the app. */
    private void registerOnClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
        View v = getLayoutInflater().inflate(R.layout.activity_register, null);

        builder.setView(v);
        AlertDialog dialog = builder.create();
        dialog.show();

        final EditText firstNameInput = v.findViewById(R.id.register_firstName);
        final EditText lastNameInput = v.findViewById(R.id.register_lastName);
        final EditText usernameInput = v.findViewById(R.id.register_username);
        final EditText passwordInput = v.findViewById(R.id.register_password);
        final EditText confirmPassInput = v.findViewById(R.id.register_confirm);
        Button reigsterBtn = v.findViewById(R.id.register_reigsterBtn);

        reigsterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Register register = new Register(StartActivity.this);

                String firstName = firstNameInput.getText().toString();
                String lastName = lastNameInput.getText().toString();
                String username = usernameInput.getText().toString();
                String password = passwordInput.getText().toString();
                String confirmPass = confirmPassInput.getText().toString();

                if (TextUtils.isEmpty(firstName) || TextUtils.isEmpty(lastName) || TextUtils.isEmpty(username)
                        || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPass)) {
                    Toast.makeText(StartActivity.this, "please fill all the fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPass)) {
                    Toast.makeText(StartActivity.this, "passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                JSONObject user = new JSONObject();

                try {
                    user.put("first_name", firstName);
                    user.put("last_name", lastName);
                    user.put("username", username);
                    user.put("password", password);
                    user.put("profile_image", "");
                    user.put("background_image", "");
                    user.put("friends", new JSONArray());
                    user.put("friend_requests", new JSONArray());
                    user.put("waiting_friend_requests", new JSONArray());

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                register.sendUser(user);

                startActivity(new Intent(StartActivity.this, MainActivity.class));
            }
        });
    }

    // Stores some user's info into shared prefs
    private void saveUserInfoIntoSharedPref(JSONObject user) {

        try {
            String firstName = user.getString("first_name");
            String lastName = user.getString("last_name");
            String username = user.getString("username");

            SharedPreferences sp = getSharedPreferences("user_info", 0);
            SharedPreferences.Editor editor = sp.edit();

            editor.putString("first_name", firstName);
            editor.putString("last_name", lastName);
            editor.putString("username", username);

            editor.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void onClick(View view) {
        if (view == loginBtn) {
            loginOnClick();
        } else if (view == registerBtn) {
            registerOnClick();
        }
    }

    private class Login extends ServerConnector {

        private JSONObject user = null;

        public Login(Context context) {
            super(context, "login");
        }

        public void sendUser(JSONObject user) {
            this.user = user;
            serverHandler.sendMessage(user.toString());
        }

        public JSONObject getUserByUsername(String username) {
            serverHandler.sendMessage("getuser");
            serverHandler.sendMessage(username);

            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String s = serverHandler.getMessage();

            try {
                JSONObject user = new JSONObject(s);
                return user;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        private boolean canLogin() {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            String s = serverHandler.getMessage();

            if (s.equals("correct")) {
                serverHandler.closeConnection();
                serverHandler = new ServerHandler(context);
                serverHandler.start();

                try {
                    user = getUserByUsername(user.getString("username"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                saveUserInfoIntoSharedPref(user);

                return true;
            }
            return false;
        }

    }

    private class Register extends ServerConnector {

        public Register(Context context) {
            super(context, "register");
        }

        public void sendUser(JSONObject user) {
            serverHandler.sendMessage(user.toString());

            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            saveUserInfoIntoSharedPref(user);

            serverHandler.closeConnection();
        }

    }
}
