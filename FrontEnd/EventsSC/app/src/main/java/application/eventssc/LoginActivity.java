package application.eventssc;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoginActivity extends AppCompatActivity {

    private String loginUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/login?";
    EditText usernameView;
    EditText passwordView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        TextView registerScreen = (TextView) findViewById(R.id.link_to_register);

        registerScreen.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(i);
            }
        });

    }

    public void submitForm(View view) {
        try {
            usernameView = (EditText) findViewById(R.id.txtUsername);
            passwordView = (EditText) findViewById(R.id.txtPassword);
            new JsonAsyncTask(usernameView.getText().toString(), passwordView.getText().toString()).execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class JsonAsyncTask extends AsyncTask<String, String, Integer> {

        String username;
        String password;

        public JsonAsyncTask(String username, String password) {
            this.username = username;
            this.password = password;
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
            url = loginUrl + "username=" + username + "&" + "password=" + password;

            InputStream resultStream;
            int result = -1;
            try {
                HttpGet request = new HttpGet(url);
                HttpResponse response = httpclient.execute(request);
                HttpEntity entity = response.getEntity();
                resultStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(resultStream, "UTF-8"), 8);

                String line;
                if ((line = reader.readLine()) != null) {
                    result = Integer.parseInt(line);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(Integer userId) {
            if (userId != -1) {
                Intent welcomeIntent = new Intent();
                welcomeIntent.setClass(LoginActivity.this, MainActivity.class);
                welcomeIntent.putExtra("UserId", userId);
                startActivity(welcomeIntent);
            } else {
                usernameView.setText(null);
                passwordView.setText(null);


                Context context = getApplicationContext();
                CharSequence text = "Invalid credentials!";
                int duration = Toast.LENGTH_SHORT;

                Toast toast = Toast.makeText(context, text, duration);
                toast.show();

                //passwordView.setError("Invalid credentials");
                usernameView.requestFocus();
            }
        }
    }

}
