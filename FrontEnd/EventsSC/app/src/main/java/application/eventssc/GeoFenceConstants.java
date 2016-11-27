package application.eventssc;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

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
import java.util.HashMap;

public class GeoFenceConstants {

    public static final String PACKAGE_NAME = "application.eventssc";

    public static final String SHARED_PREFERENCES_NAME = PACKAGE_NAME + ".SHARED_PREFERENCES_NAME";

    public static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    private static String geoFenceUrl = "http://eventssc.us-west-2.elasticbeanstalk.com/geofence";

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    public static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    public static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
    public static final float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km

    public static final HashMap<String, LatLng> LANDMARKS = new HashMap();
//    static {
//        LANDMARKS.put("Thanks giving", new LatLng(34.0281432, -118.2798475));
//        LANDMARKS.put("Trojan Walk", new LatLng(34.018811, -118.292217));
//        LANDMARKS.put("My research", new LatLng(34.0206012, -118.2860922));
//        LANDMARKS.put("Goinglobal Training Webinar", new LatLng(36.778261, -119.4179324));
//        LANDMARKS.put("On-Campus Recruiting (OCR)", new LatLng(34.0202303, -118.2857495));
//    }

    public void updateGeoFencesList(){
        try{
            FenceAsyncTask fenceAsyncTask =  new FenceAsyncTask();
            String result = fenceAsyncTask.execute().get();
            JSONArray geofenceArr = new JSONArray(result);
            for(int i=0;i<geofenceArr.length();i++){
                JSONObject geofence = (JSONObject) geofenceArr.get(i);
                LANDMARKS.put(geofence.getString("eventname"), new LatLng(geofence.getDouble("latitude"), geofence.getDouble("longitude")));
            }
        }
        catch (Exception ex){}
    }


    private class FenceAsyncTask extends AsyncTask<String, String, String> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params) {
            DefaultHttpClient httpclient = new DefaultHttpClient(new BasicHttpParams());
            HttpGet request = new HttpGet(geoFenceUrl);
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
            String jsonString;
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
        }
    }
}
