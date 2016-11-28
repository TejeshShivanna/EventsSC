package application.eventssc;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class EventDescription extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    static String eventsJsonStr = "";
    //private String setInterestUrl = "http://10.0.2.2:8080/markInterest?interestStr=";
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

        FloatingActionButton myFab = (FloatingActionButton) findViewById(R.id.fab);
        myFab.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String jsonObjString = "";
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("userId", userId);
                    obj.put("eventId", eventId);
                    obj.put("status", true);
                    jsonObjString = obj.toString();
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

        try{
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

//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
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

            return result;

        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Toast toast = Toast.makeText(getApplicationContext(), "Marked Interested", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT);
                toast.show();
            }
            Intent resultsIntent = new Intent();
            resultsIntent.setClass(getApplicationContext(), MainActivity.class);
            resultsIntent.putExtra("UserId", userId);
            startActivity(resultsIntent);
        }
    }


}