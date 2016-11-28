package eventssc.location;


import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import eventssc.dao.DaoException;
import eventssc.dao.LocationDao;
import eventssc.model.Location;
import eventssc.util.Constants;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;

public class LocationManager {

    private LocationDao locationDao;

    @Autowired
    public LocationManager(LocationDao locationDao) {
        this.locationDao = locationDao;
    }

    public int getLocationId(Location location, boolean addEntryIfAbsent) throws DaoException {
        return locationDao.getLocationId(location, addEntryIfAbsent);
    }


    public Location setLocationCoOrdinates(String address) throws Exception {
        Location location = new Location();
        try {
            GeoApiContext context = new GeoApiContext().setApiKey(Constants.GoogleGeoCodeAPI);
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
            LatLng latLng = results[0].geometry.location;
            location.setLocationName(address);
            location.setLatitude(latLng.lat);
            location.setLongitude(latLng.lng);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return location;
    }


    public String reverseGeoCode(Double latitude, Double longitude) throws Exception {
        try {
            GeoApiContext context = new GeoApiContext().setApiKey(Constants.GoogleGeoCodeAPI);
            LatLng latLng = new LatLng(latitude, longitude);
            String result = GeocodingApi.newRequest(context).latlng(latLng).await()[0].formattedAddress;
            return result;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Location setLocationAddress(Double latitude, Double longitude) throws Exception {
        Location location = new Location();
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setLocationName(reverseGeoCode(latitude, longitude));
        return location;
    }
}
