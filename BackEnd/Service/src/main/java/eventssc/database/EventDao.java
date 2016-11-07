package eventssc.database;

import eventssc.event.Event;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class EventDao {

	private static final String SQL_EVENTS_LOCATION = "SELECT latitude,longitude FROM Location where locationID =?";
	private static final String SQL_ALL_EVENTS = "SELECT * FROM Event where eventdate >= current_date ORDER BY eventdate";
	private static final String SQL_EVENT_BY_ID = "SELECT * FROM Event WHERE eventid = ?";
	private static final String SQL_INSERT_EVENT = "INSERT INTO Event(eventname, locationid, eventdescription, starttime, endtime, creator) VALUES (?,?,?,?,?,?)";
	private AmazonRDS amazonRDS;

	@Autowired
	public EventDao(AmazonRDS amazonRDS){
		this.amazonRDS = amazonRDS;
	}

	public ArrayList<Event> getAllEvents() throws DaoException {
		Connection con = null;
		Statement statement = null;
		ResultSet result = null;

		ArrayList<Event> eventList = new ArrayList<Event>();

		try {
			con = amazonRDS.getConnection();
			statement = con.createStatement();
			result = statement.executeQuery(SQL_ALL_EVENTS);

			while (result.next()) {
				eventList.add(createEventSet(result));
			}

		} catch (SQLException e) {
			throw new DaoException(e);
		}
		catch (Exception e){
			e.printStackTrace();
		}
		finally {
			try {
				con.close();
			}
			catch (SQLException e) {
				throw new DaoException(e);
			}
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

		} catch (SQLException e) {
			throw new DaoException(e);
		}
		catch (Exception e){
			e.printStackTrace();
		}finally {
			try {
				con.close();
			}
			catch (SQLException e) {
				throw new DaoException(e);
			}
		}

		return null;
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
		}
		catch (Exception e){
			e.printStackTrace();
		}finally {
			try {
				con.close();
			}
			catch (SQLException e) {
				throw new DaoException(e);
			}
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
			statement.setLong(2, jsonObj.optLong("location"));
			statement.setString(3, jsonObj.optString("description"));
			//statement.setDate(4, jsonObj.optD("starttime"));
			//statement.setDate(5, jsonObj.optString("endtime"));
			statement.setInt(6, jsonObj.optInt("creator"));

			if (statement.executeUpdate() != 0) {
				return true;
			} else {
				return false;
			}

		} catch (SQLException e) {
			throw new DaoException(e);
		} catch (JSONException e) {
			throw new DaoException(e);
		}
		catch (Exception e){
			e.printStackTrace();
		}finally {
			try {
				con.close();
			}
			catch (SQLException e) {
				throw new DaoException(e);
			}
		}
		return false;

	}

	private static Event createEventSet(ResultSet result) throws DaoException {

		Event event = null;

		try {

			event = new Event();

			event.setEventID(result.getInt("eventid"));
			event.setEventName(result.getString("eventname"));
			event.setEventDescription(result.getString("eventdescription"));
			event.setEventDate(result.getDate("eventdate"));
			String starttime = result.getString("starttime");
			try {
				SimpleDateFormat datetimeFormatter1 = new SimpleDateFormat("HH:mm:ss");
				Date stTime = datetimeFormatter1.parse(starttime);
				Date etTime = datetimeFormatter1.parse(result.getString("endtime"));
				event.setStartTime(stTime);
				event.setEndTime(etTime);
				event.setLocationID(result.getInt("locationid"));
				event.setCreator(result.getInt("creator"));
			}
			catch (ParseException pe){
				pe.printStackTrace();
			}


		} catch (SQLException e) {
			throw new DaoException(e);
		}

		return event;
	}

}
