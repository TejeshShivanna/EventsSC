package application.eventssc;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventDescription extends FragmentActivity implements OnMapReadyCallback {

    static String eventsJsonStr = "";
    private static Boolean interested = false;
    private GoogleMap mMap;
    private String setInterestUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/markInterest?interestStr=";
    private int userId;
    private int eventId = -1;
    private String eventName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_description);
        userId = getIntent().getIntExtra("UserId", -1);

        eventsJsonStr = getIntent().getStringExtra("eventObject");
        try {
            JSONObject eventObj = new JSONObject(eventsJsonStr);
            eventId = eventObj.getInt("eventID");

            TextView eventNameTv = (TextView) findViewById(R.id.idEventName);
            eventNameTv.setText(eventObj.getString("eventName"));
            eventName = eventObj.getString("eventName");

            TextView eventLocationTv = (TextView) findViewById(R.id.idEventLocation);
            eventLocationTv.setText(eventObj.getString("address"));

            TextView eventDateTv = (TextView) findViewById(R.id.idEventDate);
            eventDateTv.setText(eventObj.getString("eventDate"));

            TextView eventTimeTv = (TextView) findViewById(R.id.idEventTime);
            SimpleDateFormat sdf1 = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss a");
            SimpleDateFormat sdf2 = new SimpleDateFormat("hh:mm a");
            Date startTime = sdf1.parse(eventObj.getString("startTime"));
            Date endTime = sdf1.parse(eventObj.getString("endTime"));
            eventTimeTv.setText(sdf2.format(startTime) + " - " + sdf2.format(endTime));

            TextView eventDescriptionTv = (TextView) findViewById(R.id.idEventDescription);
            eventDescriptionTv.setText(eventObj.getString("eventDescription"));

        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fab);

        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String jsonObjString = "";
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("userId", userId);
                    obj.put("eventId", eventId);
                    obj.put("status", true);
                    jsonObjString = obj.toString();

                    if (!interested) {
                        myFab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.interested));
                        interested = true;
                    } else {
                        myFab.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.mipmap.not_interested));
                        interested = false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    String encodedString = URLEncoder.encode(jsonObjString, "UTF-8");
                    setInterestUrl += encodedString;
                    new JsonAsyncTask().execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            JSONObject eventObj = new JSONObject(eventsJsonStr);
            double latitude = eventObj.getDouble("latitude");
            double longitude = eventObj.getDouble("longitude");
            LatLng latLong = new LatLng(latitude, longitude);
            mMap.addMarker(new MarkerOptions().position(latLong).title(eventName));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLong));
            mMap.setMinZoomPreference(16);
        } catch (JSONException e) {
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
            HttpGet request = new HttpGet(setInterestUrl);
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
            Toast toast;
            if (result != null) {
                if (interested) {
                    toast = Toast.makeText(getApplicationContext(), "Event marked as interested", Toast.LENGTH_SHORT);
                } else {
                    toast = Toast.makeText(getApplicationContext(), "Event removed from interested", Toast.LENGTH_SHORT);
                }

            } else {
                toast = Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT);
            }
            if (toast != null) toast.show();
        }
    }
}