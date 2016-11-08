package eventssc.dao;

import eventssc.database.AmazonRDS;
import eventssc.model.Event;
import eventssc.util.DateUtility;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class EventDao {

    private static final String SQL_EVENTS_LOCATION = "SELECT latitude,longitude FROM Location where locationID =?";
    private static final String SQL_ALL_EVENTS = "SELECT * FROM Event ORDER BY eventdate";
    private static final String SQL_EVENT_BY_ID = "SELECT * FROM Event WHERE eventid = ?";
    private static final String SQL_INSERT_EVENT = "INSERT INTO Event(eventname, locationid, eventdescription, eventdate, starttime, endtime, creator, address) VALUES (?,?,?,?,?,?,?,?)";

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

    public boolean createEvent(String jsonStr) throws DaoException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            con = amazonRDS.getConnection();
            statement = con.prepareStatement(SQL_INSERT_EVENT);

            statement.setString(1, jsonObj.optString("name"));
            statement.setInt(2, jsonObj.optInt("locationId"));
            statement.setString(3, jsonObj.optString("description"));
            statement.setDate(4, new java.sql.Date(DateUtility.getDateFromString(jsonObj.optString("date")).getTime()));
            statement.setTime(5,
                    new java.sql.Time(DateUtility.getTimeFromString(jsonObj.optString("starttime")).getTime()));
            statement.setTime(6, new java.sql.Time(DateUtility.getTimeFromString(jsonObj.optString("endtime")).getTime()));
            statement.setInt(7, jsonObj.optInt("creatorId"));
            statement.setString(8, jsonObj.optString("address", "Default"));

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

    private static Event createEventSet(ResultSet result) throws DaoException {

        Event event = null;

        try {

            event = new Event();

            event.setEventID(result.getInt("eventid"));
            event.setEventName(result.getString("eventname"));
            event.setEventDescription(result.getString("eventdescription"));
            event.setStartTime(result.getTimestamp("starttime"));
            event.setEndTime(result.getTimestamp("endTime"));
            event.setLocationID(result.getInt("locationid"));
            event.setCreatorID(result.getInt("creator"));

        } catch (SQLException e) {
            throw new DaoException(e);
        }

        return event;
    }


}
