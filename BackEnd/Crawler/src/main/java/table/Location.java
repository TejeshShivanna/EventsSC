package table;

import com.google.maps.GeoApiContext;
import com.google.maps.GeocodingApi;
import com.google.maps.model.GeocodingResult;
import com.google.maps.model.LatLng;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resource.Constants;

public class Location {

    private String address;

    private Double latitude;

    private Double longitude;

    final static Logger logger = LoggerFactory.getLogger(Location.class);

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public void getLocationCoOrdinates(String address) throws Exception{
        try {
            GeoApiContext context = new GeoApiContext().setApiKey(Constants.GoogleGeoCodeAPI);
            GeocodingResult[] results = GeocodingApi.geocode(context, address).await();
            LatLng latLng = results[0].geometry.location;
            this.address = address;
            this.latitude = latLng.lat;
            this.longitude = latLng.lng;
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
    }
}