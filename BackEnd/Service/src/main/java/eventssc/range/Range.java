package eventssc.range;

import com.google.gson.Gson;
import eventssc.dao.DaoException;
import eventssc.event.EventManager;
import eventssc.model.Event;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

public class Range {

    private EventManager eventManager;

    @Autowired
    public Range(EventManager eventManager){
        this.eventManager =eventManager;
    }

    public String getEventsinRange(String latLong) throws DaoException{

        try {
            JSONObject cord = new JSONObject(latLong);
            double latitude = cord.optDouble("latitude");
            double longitude = cord.optDouble("longitude");
            double defaultDist = cord.optDouble("range");
            List<Event> ListOfEvents = eventManager.getTodaysEvents();

            ArrayList<Event> nearbyEvents = new ArrayList<Event>();
            for (Event event : ListOfEvents) {
                double location[] = eventManager.getLocationById(event.getLocationID());
                event.setLatitude(location[0]);
                event.setLongitude(location[1]);
                double dist = distance(latitude, longitude, event.getLatitude(), event.getLongitude());
                if (dist <= defaultDist) {
                    nearbyEvents.add(event);
                }
            }
            Gson gson = new Gson();
            return gson.toJson(nearbyEvents);

        }
        catch (DaoException de){
            de.printStackTrace();
            return "[{abc}]";
        } catch (JSONException e) {
            return "[{abc}]";
        }

    }
    private static double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return (dist);
    }

    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}
