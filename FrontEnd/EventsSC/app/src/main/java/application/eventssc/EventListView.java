package application.eventssc;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;

public class EventListView extends AppCompatActivity {

    private String webServerUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/all_events";
    private String isInterestedDefaultUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/isInterested?interestStr=";
    private String isInterestedUrl;

    static String eventsJsonStr = "";
    static JSONArray jsonArr;
    private JSONObject obj;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_list_view);

        eventsJsonStr = getIntent().getStringExtra("eventsJsonString");
        final ArrayList<String> list = new ArrayList<String>();

        try{
            jsonArr = new JSONArray(eventsJsonStr);
            for(int i = 0; i < jsonArr.length(); i++){
                JSONObject obj = (JSONObject) jsonArr.get(i);
                list.add(obj.optString("eventName", "") + " \n " + obj.optString("eventDate", ""));
            }
        } catch (JSONException e){
            e.printStackTrace();
        }



        final ListView listview = (ListView) findViewById(R.id.listview);

        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, list);

        listview.setClickable(true);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Object item = listview.getItemAtPosition(position);
                Toast.makeText(getApplicationContext(), item.toString(), Toast.LENGTH_SHORT).show();
                try {
                    obj = new JSONObject();
                    obj = (JSONObject) jsonArr.get(position);

                    JSONObject interestedJson = new JSONObject();
                    interestedJson.put("userId", MapsActivity.userId);
                    interestedJson.put("eventId", Integer.parseInt(obj.get("eventID").toString()));

                    String encodedString = URLEncoder.encode(interestedJson.toString(), "UTF-8");
                    isInterestedUrl = isInterestedDefaultUrl + encodedString;

                    new EventListAsyncTask().execute();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        listview.setAdapter(adapter);


    }

    private class EventListAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet request = new HttpGet(isInterestedUrl);
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
            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            EventDescription.interested = (result.contains("true"))?true:false;

            Intent resultsIntent = new Intent();
            resultsIntent.setClass(getApplicationContext(), EventDescription.class);
            resultsIntent.putExtra("eventObject", obj.toString());
            startActivity(resultsIntent);
        }
    }

}
