package pe.saydomonkos.lightweightgps;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class LoginRegisterActivity extends AppCompatActivity {

    //login ui
    EditText loginName;
    EditText loginPassword;
    Button login;
    Button registerInstead;

    //register ui
    EditText username;
    EditText email;
    EditText newPassword;
    Button register;
    Button loginInstead;

    String token;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        changeToLogin();
    }

    void changeToLogin() {
        setContentView(R.layout.login_layout);

        loginName = findViewById(R.id.editUserEmail);
        loginPassword = findViewById(R.id.editPassword);
        login = findViewById(R.id.loginBtn);
        registerInstead = findViewById(R.id.registerInsteadBtn);

        registerInstead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeToRegister();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                token = null;
                String usernameOrEmail = String.valueOf(loginName.getText());
                String password = String.valueOf(loginPassword.getText());

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL("http://87.229.85.225:42069/users/login");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                            conn.setRequestProperty("Accept","application/json");
                            conn.setDoOutput(true);
                            conn.setDoInput(true);

                            JSONObject jsonParam = new JSONObject();
                            jsonParam.put("email", usernameOrEmail);
                            jsonParam.put("password", password);

                            Log.i("JSON", jsonParam.toString());
                            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                            os.writeBytes(jsonParam.toString());

                            os.flush();
                            os.close();

                            if (conn.getResponseCode() == 200) {
                                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                StringBuilder sb = new StringBuilder();
                                String line;
                                while ((line = br.readLine()) != null) {
                                    sb.append(line+"\n");
                                }
                                br.close();
                                String response = sb.toString();
                                if (response.contains("token")) {
                                    response = response.replace("{","")
                                            .replace("}","")
                                            .replace("\"token\":", "")
                                            .replace("\"","");
                                    setToken(response);
                                }
                            }

                            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                            Log.i("MSG" , conn.getResponseMessage());

                            conn.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (token != null) {
                    //Toast.makeText(LoginRegisterActivity.this, token, Toast.LENGTH_SHORT).show();
                } else {
                    thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL("http://87.229.85.225:42069/users/login");
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                                conn.setRequestProperty("Accept","application/json");
                                conn.setDoOutput(true);
                                conn.setDoInput(true);

                                JSONObject jsonParam = new JSONObject();
                                jsonParam.put("username", usernameOrEmail);
                                jsonParam.put("password", password);

                                Log.i("JSON", jsonParam.toString());
                                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                                os.writeBytes(jsonParam.toString());

                                os.flush();
                                os.close();

                                if (conn.getResponseCode() == 200) {
                                    BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                    StringBuilder sb = new StringBuilder();
                                    String line;
                                    while ((line = br.readLine()) != null) {
                                        sb.append(line+"\n");
                                    }
                                    br.close();
                                    String response = sb.toString();
                                    if (response.contains("token")) {
                                        response = response.replace("{","")
                                                .replace("}","")
                                                .replace("\"token\":", "")
                                                .replace("\"","");
                                        setToken(response);
                                        Log.i("token",response);
                                    }
                                }

                                Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                                Log.i("MSG" , conn.getResponseMessage());

                                conn.disconnect();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    thread.start();
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                if (token != null) {
                    Intent userLoggedScreen = new Intent(LoginRegisterActivity.this, UserActivity.class);
                    userLoggedScreen.putExtra("token", token);
                    LoginRegisterActivity.this.startActivity(userLoggedScreen);
                } else {
                    Toast.makeText(LoginRegisterActivity.this, "invalid login credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    boolean registrationSuccessful;
    void changeToRegister() {
        setContentView(R.layout.register_layout);

        username = findViewById(R.id.editUser);
        email = findViewById(R.id.editEmail);
        newPassword = findViewById(R.id.editNewPassword);
        register = findViewById(R.id.registerBtn);
        loginInstead = findViewById(R.id.loginInsteadBtn);

        loginInstead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeToLogin();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String user = String.valueOf(username.getText());
                String mail = String.valueOf(email.getText());
                String newpw = String.valueOf(newPassword.getText());
                registrationSuccessful = false;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL("http://87.229.85.225:42069/signup");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
                            conn.setRequestProperty("Accept","application/json");
                            conn.setDoOutput(true);
                            conn.setDoInput(true);

                            JSONObject jsonParam = new JSONObject();
                            jsonParam.put("username", user);
                            jsonParam.put("email", mail);
                            jsonParam.put("password", newpw);

                            Log.i("JSON", jsonParam.toString());
                            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                            os.writeBytes(jsonParam.toString());

                            os.flush();
                            os.close();

                            if (conn.getResponseCode() == 200) {
                                setRegistrationSuccessful(true);
                            }

                            Log.i("STATUS", String.valueOf(conn.getResponseCode()));
                            Log.i("MSG" , conn.getResponseMessage());

                            conn.disconnect();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (registrationSuccessful) {
                    doToast("Registration successful");
                } else {
                    doToast("Error while creating you account");
                }
            }
        });
    }

    void setToken(String token) {
        this.token = token;
    }

    void doToast(String message) {
        Toast.makeText(LoginRegisterActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    void setRegistrationSuccessful(boolean result) {
        registrationSuccessful = result;
    }
}
