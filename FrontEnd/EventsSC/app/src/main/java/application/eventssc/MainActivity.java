package application.eventssc;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private String webServerUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/all_events";
    private String geofenceUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/geofence";

    private String jsonString = "";
    private String geofencejsonString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void submitForm(View view) {
        try {
            new JsonAsyncTask().execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void submitGeoFenceForm(View view) {
        try {
            new GeoFenceJsonAsyncTask().execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class JsonAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet request = new HttpGet(webServerUrl);
            InputStream resultStream = null;
            String result = null;
            try {
                HttpResponse response = httpclient.execute(request);
                HttpEntity entity = response.getEntity();
                resultStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(resultStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                result = sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONArray jsonArr;

            try {
                jsonArr = new JSONArray(result != null ? result : "");
                jsonString = jsonArr.toString();
                Log.d("MainActivity", "json" + jsonString);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            Intent resultsIntent = new Intent();
            resultsIntent.setClass(getApplicationContext(), EventListView.class);
            resultsIntent.putExtra("eventsJsonString", jsonString);
            startActivity(resultsIntent);
        }
    }

    private class GeoFenceJsonAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet request = new HttpGet(geofenceUrl);
            InputStream resultStream;
            String result = null;
            try {
                HttpResponse response = httpclient.execute(request);
                HttpEntity entity = response.getEntity();
                resultStream = entity.getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(resultStream, "UTF-8"), 8);
                StringBuilder sb = new StringBuilder();

                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line + "\n");
                }
                result = sb.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }
            JSONArray jsonArr;

            try {
                jsonArr = new JSONArray(result != null ? result : "");
                geofencejsonString = jsonArr.toString();
                Log.d("MainActivity", "json" + geofencejsonString);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            Intent resultsIntent = new Intent();
            resultsIntent.setClass(getApplicationContext(), GeoFenceActivity.class);
            resultsIntent.putExtra("geofenceString", geofencejsonString);
            startActivity(resultsIntent);
        }
    }
}
