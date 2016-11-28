package eventssc.dao;

import eventssc.database.AmazonRDS;
import eventssc.location.LocationManager;
import eventssc.model.Event;
import eventssc.model.Location;
import eventssc.model.User;
import eventssc.util.DateUtility;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class EventDao {

    private static final String SQL_EVENTS_LOCATION = "SELECT latitude,longitude FROM Location where locationID =?";
    private static final String SQL_ALL_EVENTS = "SELECT * FROM Event where eventdate >= ((SELECT NOW() AT TIME ZONE 'PST') ::timestamp::date)";
    private static final String SQL_TODAY_EVENTS = "SELECT * FROM Event where eventdate = ((SELECT NOW() AT TIME ZONE 'PST') ::timestamp::date) AND ENDTIME>=(SELECT ((SELECT NOW() AT TIME ZONE 'PST') ::timestamp::time)) ORDER BY eventdate";
    private static final String SQL_EVENT_BY_ID = "SELECT * FROM Event WHERE eventid = ?";

    private static final String SQL_INSERT_EVENT = "INSERT INTO Event(eventname, locationid, eventdescription, eventdate, starttime, endtime, creator, address) VALUES (?,?,?,?,?,?,?,?)";
    private static final String SQL_INSERT_RSVP = "INSERT INTO Rsvp(userid, eventid, status) VALUES (?,?,?)";

    private static final String SQL_GET_INTERESTED_EVENTS = "SELECT * FROM Event e INNER JOIN Rsvp r ON e.eventid = r.eventid WHERE r.status=true AND r.userid = ? AND e.eventdate >= ((SELECT NOW() AT TIME ZONE 'PST') ::timestamp::date)";
    private static final String SQL_GET_CREATED_EVENTS = "SELECT * FROM Event WHERE creator = ? AND eventdate >= ((SELECT NOW() AT TIME ZONE 'PST') ::timestamp::date)";

    private static final String SQL_GET_USERS_INTERESTED = "SELECT firstname, lastname FROM Userdetails u INNER JOIN Rsvp r ON u.userid = r.userid WHERE r.status=true AND r.eventid = ?";

    private AmazonRDS amazonRDS;

    @Autowired
    public EventDao(AmazonRDS amazonRDS) {
        this.amazonRDS = amazonRDS;
    }

    public List<Event> getAllEvents() throws DaoException {
        Connection con = null;
        Statement statement = null;
        ResultSet result = null;

        List<Event> eventList = new ArrayList<Event>();

        try {
            con = amazonRDS.getConnection();
            statement = con.createStatement();
            result = statement.executeQuery(SQL_ALL_EVENTS);

            while (result.next()) {
                eventList.add(createEventSet(result));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            amazonRDS.close(result, statement);
        }

        return eventList;
    }

    public List<Event> getInterestedEvents(int userId) throws DaoException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        List<Event> eventList = new ArrayList<Event>();

        try {
            con = amazonRDS.getConnection();
            statement = con.prepareStatement(SQL_GET_INTERESTED_EVENTS);
            statement.setLong(1, userId);
            result = statement.executeQuery();

            while (result.next()) {
                eventList.add(createEventSet(result));
            }
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            amazonRDS.close(result, statement);
        }
        return eventList;
    }

    public List<Event> getCreatedEvents(int userId) throws DaoException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        List<Event> eventList = new ArrayList<Event>();

        try {
            con = amazonRDS.getConnection();
            statement = con.prepareStatement(SQL_GET_CREATED_EVENTS);
            statement.setLong(1, userId);
            result = statement.executeQuery();

            while (result.next()) {
                eventList.add(createEventSet(result));
            }
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            amazonRDS.close(result, statement);
        }
        return eventList;
    }


    private static Event createEventSet(ResultSet result) throws DaoException {

        Event event = null;

        try {

            event = new Event();

            event.setEventID(result.getInt("eventid"));
            event.setEventName(result.getString("eventname"));
            event.setEventDescription(result.getString("eventdescription"));
            event.setLocationID(result.getInt("locationid"));
            event.setCreatorID(result.getInt("creator"));
            event.setAddress(result.getString("address"));
            event.setEventDate(result.getDate("eventdate"));
            String startTime = result.getDate("eventdate").toString() + " " + result.getString("starttime");
            String endTime = result.getDate("eventdate").toString() + " " + result.getString("endtime");

            try {
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date stTime = sdf1.parse(startTime);
                Date etTime = sdf1.parse(endTime);
                event.setStartTime(stTime);
                event.setEndTime(etTime);
            } catch (ParseException pe) {
                pe.printStackTrace();
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }

        return event;
    }

    public List<Event> getTodaysEvents() throws DaoException {
        Connection con;
        Statement statement = null;
        ResultSet result = null;

        List<Event> eventList = new ArrayList<Event>();

        try {
            con = amazonRDS.getConnection();
            statement = con.createStatement();
            result = statement.executeQuery(SQL_TODAY_EVENTS);

            while (result.next()) {
                eventList.add(createEventSet(result));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            amazonRDS.close(result, statement);
        }

        return eventList;
    }

    public Event getEventById(long eventID) throws DaoException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            con = amazonRDS.getConnection();
            statement = con.prepareStatement(SQL_EVENT_BY_ID);
            statement.setLong(1, eventID);
            result = statement.executeQuery();

            if (result.next()) {
                return createEventSet(result);
            }
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            amazonRDS.close(result, statement);
        }

        return null;
    }

    public boolean markInterest(String jsonStr) throws DaoException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            con = amazonRDS.getConnection();
            statement = con.prepareStatement(SQL_INSERT_RSVP);
            statement.setInt(1, jsonObj.optInt("userId"));
            statement.setInt(2, jsonObj.optInt("eventId"));
            statement.setBoolean(3, jsonObj.optBoolean("status"));
            if (statement.executeUpdate() != 0) {
                con.commit();
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            amazonRDS.close(result, statement);
        }
    }

    public boolean createEventWithLatLong(String jsonStr) throws DaoException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        LocationDao ld = new LocationDao(amazonRDS);
        LocationManager lm = new LocationManager(ld);

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            con = amazonRDS.getConnection();
            statement = con.prepareStatement(SQL_INSERT_EVENT);

            String address = jsonObj.optString("address", "");
            int locationId = -1;
            if (address.equals("")) {
                Double latitude = jsonObj.optDouble("latitude");
                Double longitude = jsonObj.optDouble("longitude");
                Location location = lm.setLocationAddress(latitude, longitude);
                locationId = lm.getLocationId(location, true);
            } else {
                Location location = lm.setLocationCoOrdinates(address);
                locationId = lm.getLocationId(location, true);
            }

            statement.setString(1, jsonObj.optString("name"));
            statement.setInt(2, locationId);
            statement.setString(3, jsonObj.optString("description"));
            statement.setDate(4, new java.sql.Date(DateUtility.getDateFromApp(jsonObj.optString("date")).getTime()));
            statement.setTime(5, new java.sql.Time(DateUtility.getTimeFromApp(jsonObj.optString("starttime")).getTime()));
            statement.setTime(6, new java.sql.Time(DateUtility.getTimeFromApp(jsonObj.optString("endtime")).getTime()));
            statement.setInt(7, jsonObj.optInt("creatorId"));
            statement.setString(8, address);

            if (statement.executeUpdate() != 0) {
                con.commit();
                return true;
            } else {
                return false;
            }


        } catch (SQLException e) {
            throw new DaoException(e);
        } catch (JSONException e) {
            throw new DaoException(e);
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            amazonRDS.close(result, statement);
        }
    }

    public boolean createEvent(String jsonStr) throws DaoException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        LocationDao ld = new LocationDao(amazonRDS);
        LocationManager lm = new LocationManager(ld);

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            con = amazonRDS.getConnection();
            statement = con.prepareStatement(SQL_INSERT_EVENT);

            String address = jsonObj.optString("address", "");
            Location location = lm.setLocationCoOrdinates(address);

            int locationId = lm.getLocationId(location, true);

            statement.setString(1, jsonObj.optString("name"));
            statement.setInt(2, locationId);
            statement.setString(3, jsonObj.optString("description"));
            statement.setDate(4, new java.sql.Date(DateUtility.getDateFromApp(jsonObj.optString("date")).getTime()));
            statement.setTime(5, new java.sql.Time(DateUtility.getTimeFromApp(jsonObj.optString("starttime")).getTime()));
            statement.setTime(6, new java.sql.Time(DateUtility.getTimeFromApp(jsonObj.optString("endtime")).getTime()));
            statement.setInt(7, jsonObj.optInt("creatorId"));
            statement.setString(8, jsonObj.optString("address", "USC"));

            if (statement.executeUpdate() != 0) {
                con.commit();
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } catch (JSONException e) {
            throw new DaoException(e);
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            amazonRDS.close(result, statement);
        }
    }

    public double[] getLocationById(int locationID) throws DaoException {

        Connection con = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        double arr[] = new double[2];
        try {
            con = amazonRDS.getConnection();
            statement = con.prepareStatement(SQL_EVENTS_LOCATION);
            statement.setInt(1, locationID);
            result = statement.executeQuery();
            if (result.next()) {
                arr[0] = result.getDouble("latitude");
                arr[1] = result.getDouble("longitude");
                return arr;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                throw new DaoException(e);
            }
        }
        return null;
    }

    public List<String> getUsersInterested(int eventId) throws DaoException {
        List<String> userList = new ArrayList<>();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            con = amazonRDS.getConnection();
            statement = con.prepareStatement(SQL_GET_USERS_INTERESTED);
            statement.setInt(1, eventId);
            result = statement.executeQuery();
            while (result.next()) {
                userList.add(result.getString("firstname") + " " + result.getString("lastname"));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException e) {
                throw new DaoException(e);
            }
        }
        return userList;
    }
}
