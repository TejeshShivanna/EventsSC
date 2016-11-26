package eventssc.location;


import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import eventssc.dao.DaoException;
import eventssc.dao.LocationDao;
import eventssc.model.Location;
import eventssc.util.Constants;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Created by Sharath_GM on 11/12/16.
 */
public class LocationManager {

    private LocationDao locationDao;

//    public LocationManager() {
//        this.locationDao = null;
//    }

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

}
