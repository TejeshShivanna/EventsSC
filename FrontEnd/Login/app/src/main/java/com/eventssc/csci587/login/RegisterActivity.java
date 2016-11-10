package com.eventssc.csci587.login;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;

public class RegisterActivity extends AppCompatActivity {

    EditText firstNameView;
    EditText lastNameView;
    EditText userNameView;
    EditText passwordView;
    EditText phoneView;
    private String registerUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/registeruser?user=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        TextView loginScreen = (TextView) findViewById(R.id.link_to_login);

        loginScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                finish();
            }
        });
    }

    public void submitForm(View view) {
        try {
            firstNameView = (EditText) findViewById(R.id.txt_firstName);
            lastNameView = (EditText) findViewById(R.id.txt_lastName);
            userNameView = (EditText) findViewById(R.id.txt_userName);
            passwordView = (EditText) findViewById(R.id.txt_password);
            phoneView = (EditText) findViewById(R.id.txt_phone);

            new RegisterActivity.JsonAsyncTask(firstNameView.getText().toString(), lastNameView.getText().toString(),
                    userNameView.getText().toString(), passwordView.getText().toString(), phoneView.getText().toString()).execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class JsonAsyncTask extends AsyncTask<String, String, Integer> {

        String firstName;
        String lastName;
        String userName;
        String password;
        String phone;

        public JsonAsyncTask(String firstName, String lastName, String username, String password, String phone){
            this.firstName = firstName;
            this.lastName = lastName;
            this.userName = username;
            this.password = password;
            this.phone = phone;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(String... params) {
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            String url;

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("firstname", firstName);
                jsonObject.put("lastname", lastName);
                jsonObject.put("username", userName);
                jsonObject.put("password", password);
                jsonObject.put("phone", phone);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            url = registerUrl + URLEncoder.encode(jsonObject.toString());
            InputStream resultStream;
            int result = -1;
            try {
                HttpGet request = new HttpGet(url);
                HttpResponse response = httpclient.execute(request);
                HttpEntity entity = response.getEntity();
                resultStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(resultStream, "UTF-8"), 8);

                String line;
                if((line = reader.readLine()) != null) {
                    result = Integer.parseInt(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer userId) {
            if(userId != -1){
                Intent welcomeIntent = new Intent();
                welcomeIntent.setClass(RegisterActivity.this, WelcomeScreen.class);
                welcomeIntent.putExtra("UserId", userId);
                startActivity(welcomeIntent);
            }
            else{
                firstNameView.setText(null);
                lastNameView.setText(null);
                userNameView.setText(null);
                passwordView.setText(null);
                phoneView.setText(null);

                firstNameView.setError("Invalid details, please enter again!");
                firstNameView.requestFocus();
            }
        }
    }
}
