package application.eventssc;


import android.*;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
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

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener,GoogleMap.OnInfoWindowClickListener,SeekBar.OnSeekBarChangeListener{

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    Marker mCurrLocationMarker;
    LocationRequest mLocationRequest;
    static String eventsJsonStr = "";
    private String webServerUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/range?latLong=";
    private String allEventsUrl="http://eventssc.us-west-2.elasticbeanstalk.com/all_events";
    private String jsonString = "";
    SeekBar seekBar;
    ArrayList<MarkerOptions> allMarkers = new ArrayList<MarkerOptions>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }
        seekBar = (SeekBar)findViewById(R.id.seekBarRange);
        seekBar.setProgress(2);
        seekBar.setOnSeekBarChangeListener(this);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        double range = ((double)seekBar.getProgress())/10;
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker.remove();
        }
        mMap.clear();
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

}
