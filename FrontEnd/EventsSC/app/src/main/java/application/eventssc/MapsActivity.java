package application.eventssc;


import android.*;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
import java.util.ArrayList;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,GoogleMap.OnInfoWindowClickListener,SeekBar.OnSeekBarChangeListener, AdapterView.OnItemClickListener,ResultCallback<Status> {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    private ListView mDrawerList;
    private ArrayAdapter<String> mAdapter;
    static String eventsJsonStr = "";
    private String webServerUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/range?latLong=";
    private String allEventsUrl="http://eventssc.us-west-2.elasticbeanstalk.com/all_events";
    private String jsonString = "";
    private String userNameUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/getUser?userIdStr=";
    private String InterestedUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/range?latLong=";
    private String CreatedUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/range?latLong=";
    SeekBar seekBar;
    ArrayList<MarkerOptions> allMarkers = new ArrayList<MarkerOptions>();


    protected static final String TAG = "GeoFenceActivity";


    protected ArrayList<Geofence> mGeofenceList;

    private boolean mGeofencesAdded;

    private PendingIntent mGeofencePendingIntent;

    private SharedPreferences mSharedPreferences;

    private GeoFenceConstants geoFenceConstants;
    int userId;








    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        userId = getIntent().getIntExtra("UserId", -1);
        seekBar = (SeekBar)findViewById(R.id.seekBarRange);
        seekBar.setProgress(2);
        seekBar.setOnSeekBarChangeListener(this);
        mDrawerList = (ListView)findViewById(R.id.left_drawer);
        addDrawerItems();
        mDrawerList.setOnItemClickListener(this);


        // Empty list for storing geofences.
        mGeofenceList = new ArrayList();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        geoFenceConstants = new GeoFenceConstants();

        // Retrieve an instance of the SharedPreferences object.
        mSharedPreferences = getSharedPreferences(GeoFenceConstants.SHARED_PREFERENCES_NAME,
                MODE_PRIVATE);

        // Get the value of mGeofencesAdded from SharedPreferences. Set to false as a default.
        mGeofencesAdded = mSharedPreferences.getBoolean(GeoFenceConstants.GEOFENCES_ADDED_KEY, false);

        // Get the geofences used. Geofence data is hard coded in this sample.
        populateGeofenceList();


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void addDrawerItems() {
        String userName = "Hello, " + getUserName(userId);
        String[] osArray = { userName,"Home", "Events Interested In", "Create Event", "Event created by You"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);
    }


    public void addGeofences() {
        populateGeofenceList();
        if (!mGoogleApiClient.isConnected()) {
            Toast.makeText(this, getString(R.string.not_connected), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            if(getGeofencingRequest()!=null) {
                LocationServices.GeofencingApi.addGeofences(
                        mGoogleApiClient,
                        // The GeofenceRequest object

                        getGeofencingRequest(),
                        // A pending intent that that is reused when calling removeGeofences(). This
                        // pending intent is used to generate an intent when a matched geofence
                        // transition is observed.
                        getGeofencePendingIntent()
                ).setResultCallback(this);
            }// Result processed in onResult().
        } catch (SecurityException securityException) {
        }
    }


    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void populateGeofenceList() {

        geoFenceConstants.updateGeoFencesList();
        for (Map.Entry<String, LatLng> entry : GeoFenceConstants.LANDMARKS.entrySet()) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            GeoFenceConstants.GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(GeoFenceConstants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }
    }


    private GeofencingRequest getGeofencingRequest() {
        try {
            GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

            // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
            // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
            // is already inside that geofence.
            builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);

            // Add the geofences to be monitored by geofencing service.
            builder.addGeofences(mGeofenceList);

            // Return a GeofencingRequest.
            return builder.build();
        }
        catch (Exception e){

        }
        return null;
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);

            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public void listView(View view) {
        try {
            new MapsActivity.ListAsyncTask().execute();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String submitForm(double lat,double lon, double range) {
        try {

            JsonAsyncTask asyncTask =  new JsonAsyncTask(lat,lon,range);
            String result = asyncTask.execute().get();
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public String getUserName(int uid) {
        try {

            UserAsyncTask userAsyncTask=new UserAsyncTask(uid);
            String result = userAsyncTask.execute().get();
            return result;

        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        double range = ((double)seekBar.getProgress())/10;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        mMap.clear();
        addGeofences();
        if(!allMarkers.isEmpty())
        {
            for (int i =0;i<allMarkers.size();i++){
                allMarkers.remove(i);
            }
        }



        eventsJsonStr = submitForm(location.getLatitude(),location.getLongitude(),range);
        //Place current location marker
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("Current Position");
        try{
            //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            //mCurrLocationMarker = mMap.addMarker(markerOptions);

            JSONArray jsonArr = new JSONArray(eventsJsonStr);
            for(int i = 0; i < jsonArr.length(); i++){
                JSONObject obj = (JSONObject) jsonArr.get(i);
                int flag = 0;
                for(int j = 0;j<i;j++){
                    JSONObject temp = (JSONObject) jsonArr.get(j);
                    if(temp.getDouble("latitude") == obj.getDouble("latitude") && temp.getDouble("longitude") == obj.getDouble("longitude")){
                        flag=1;
                        break;
                    }
                }
                LatLng sydney;
                if(flag == 0) {
                    sydney = new LatLng(obj.getDouble("latitude"), obj.getDouble("longitude"));
                }
                else {
                    obj.put("latitude",obj.getDouble("latitude")+0.00005);
                    obj.put("longitude",obj.getDouble("longitude")+0.00005);
                    jsonArr.put(i,(Object)obj);
                    sydney = new LatLng(obj.getDouble("latitude"), obj.getDouble("longitude"));
                }
                MarkerOptions mark = new MarkerOptions().position(sydney).title(obj.getString("eventName"));
                allMarkers.add(mark);
                mMap.addMarker(mark);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        mMap.setOnInfoWindowClickListener(this);

        //stop location updates
        if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        }

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String title = marker.getTitle();
        try{
            //markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN));
            //mCurrLocationMarker = mMap.addMarker(markerOptions);

            JSONArray jsonArr = new JSONArray(eventsJsonStr);
            for(int i = 0; i < jsonArr.length(); i++){
                JSONObject obj = (JSONObject) jsonArr.get(i);
                if(obj.getString("eventName").equals(title)){
                    Toast.makeText(this, obj.getString("eventName"), Toast.LENGTH_LONG).show();
                    Intent resultsIntent = new Intent();
                    resultsIntent.setClass(getApplicationContext(), EventDescription.class);
                    resultsIntent.putExtra("eventObject", obj.toString());
                    startActivity(resultsIntent);
                    break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public boolean checkLocationPermission(){
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Asking user if explanation is needed
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

                //Prompt the user once explanation has been shown
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                } else {

                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }

            // other 'case' lines to check for other permissions this app might request.
            // You can add here other case statements according to your requirement.
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        Toast.makeText(getApplicationContext(),"Range: "+(double)(seekBar.getProgress())/10+" Mile(s)", Toast.LENGTH_SHORT).show();
        onLocationChanged(mLastLocation);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(position==0){

        }
        else if(position==1){
            Intent resultsIntent = new Intent();
            resultsIntent.setClass(getApplicationContext(), MapsActivity.class);
            resultsIntent.putExtra("UserId", userId);
            startActivity(resultsIntent);
        }
        else if(position==2){
            Intent resultsIntent = new Intent();
            resultsIntent.setClass(getApplicationContext(), InterestedEvents.class);
            resultsIntent.putExtra("UserId", userId);
            startActivity(resultsIntent);
        }
        else if(position==3){
            Intent resultsIntent = new Intent();
            resultsIntent.setClass(getApplicationContext(), CreateEvent.class);
            resultsIntent.putExtra("UserId", userId);
            startActivity(resultsIntent);
        }
        else if(position==4){
            Intent resultsIntent = new Intent();
            resultsIntent.setClass(getApplicationContext(), EventsByYou.class);
            resultsIntent.putExtra("UserId", userId);
            startActivity(resultsIntent);
        }

    }

    @Override
    public void onResult(Status status) {
        if (status.isSuccess()) {
            // Update state and save in shared preferences.
            mGeofencesAdded = !mGeofencesAdded;
            SharedPreferences.Editor editor = mSharedPreferences.edit();
            editor.putBoolean(GeoFenceConstants.GEOFENCES_ADDED_KEY, mGeofencesAdded);
            editor.apply();

            // Update the UI. Adding geofences enables the Remove Geofences button, and removing
            // geofences enables the Add Geofences button.

        } else {

        }
    }
    private class JsonAsyncTask extends AsyncTask<String, String, String> {
        double latitude;
        double longitude;
        double range;

        public JsonAsyncTask(double latitude,double longitude, double range){
            this.latitude = latitude;
            this.longitude = longitude;
            this.range = range;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());

            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("latitude", latitude);
                jsonObject.put("longitude", longitude);
                jsonObject.put("range",range);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            String url = webServerUrl + URLEncoder.encode(jsonObject.toString());
            HttpGet request = new HttpGet(url);
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
                Log.d("MapsActivity", "json" + jsonString);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return result;

        }

        @Override
        protected void onPostExecute(String result) {
        }
    }


    private class ListAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet request = new HttpGet(allEventsUrl);
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

    private class UserAsyncTask extends AsyncTask<String, String, String> {
        int uid;

        public UserAsyncTask(int uid){
            this.uid =uid;
        }

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            String url = userNameUrl + uid;
            HttpGet request = new HttpGet(url);
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
        }
    }


}
