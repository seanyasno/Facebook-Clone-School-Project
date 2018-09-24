package com.yasnosean.schoolprojectapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class StartActivity extends AppCompatActivity {

    private Button loginBtn;
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        loginBtn = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        handleFunctions();
    }

    private void handleFunctions() {
        loginBtn.setOnClickListener(loginOnClick);
        registerBtn.setOnClickListener(registerOnClick);
    }

    private View.OnClickListener loginOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            AlertDialog.Builder builder = new AlertDialog.Builder(StartActivity.this);
            View v = getLayoutInflater().inflate(R.layout.activity_login, null);

            builder.setView(v);
            AlertDialog dialog = builder.create();
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

                    // sending the user data to the server to get response
                    // if it is correct or incorrect
                    login.sendUser(user);

                    // if the user can login, it will load up the next activity
                    // if not so it will make a toast
                    if (!login.canLogin()) {
                        Toast.makeText(StartActivity.this, "username or password is incorrect", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    };

    private View.OnClickListener registerOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
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
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    register.sendUser(user);

                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                }
            });
        }
    };

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

    private class Login implements IServerHandler {

        private ServerHandler serverHandler;
        private JSONObject user = null;

        private Context context;

        public Login(Context context) {
            this.context = context;
            serverHandler = new ServerHandler(context, WhichSocketListener.LOGIN);
            serverHandler.start();
            serverHandler.sendMessage("login");
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

            String s = serverHandler.getMessager();

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
            String s = serverHandler.getMessager();

            if (s.equals("correct")) {
                serverHandler.closeConnection();
                serverHandler = new ServerHandler(context, WhichSocketListener.LOGIN);
                serverHandler.start();

                try {
                    user = getUserByUsername(user.getString("username"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                saveUserInfoIntoSharedPref(user);

                startActivity(new Intent(StartActivity.this, MainActivity.class));
                finish();
                return true;
            }
            return false;
        }

    }

    private class Register implements IServerHandler {

        private ServerHandler serverHandler;
        private JSONObject user;

        public Register(Context context) {
            serverHandler = new ServerHandler(context, WhichSocketListener.REGISTER);
            serverHandler.start();
            serverHandler.sendMessage("register");
        }

        public void sendUser(JSONObject user) {
            this.user = user;
            serverHandler.sendMessage(user.toString());

            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            saveUserInfoIntoSharedPref(user);

            serverHandler.closeConnection();
        }

        public JSONObject getUserByUsername(String username) {
            return user;
        }

    }
}
