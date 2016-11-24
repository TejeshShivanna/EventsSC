package eventssc.dao;

import eventssc.database.AmazonRDS;
import eventssc.model.Location;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.*;

/**
 * Created by Sharath_GM on 11/12/16.
 */
public class LocationDao {

    private static final String SQL_GET_LOCATION_ID = "SELECT locationid FROM Location where latitude = ? AND longitude = ?";
    private static final String SQL_INSERT_LOCATION = "INSERT INTO LOCATION(locationname, latitude, longitude) VALUES (?,?,?)";


    private AmazonRDS amazonRDS;

    @Autowired
    public LocationDao(AmazonRDS amazonRDS) {
        this.amazonRDS = amazonRDS;
    }

    public int getLocationId(Location location, Boolean addEntryIfAbsent) throws DaoException {
        int locationId = getLocationID(location);
        if(locationId == -1 && addEntryIfAbsent){
            locationId = addLocation(location);
        }
        return locationId;
    }

    public int getLocationID(Location location) throws DaoException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet result = null;

        int locationID = -1;

        try {
            con = amazonRDS.getConnection();
            statement = con.prepareStatement(SQL_GET_LOCATION_ID);
            statement.setDouble(1, location.getLatitude());
            statement.setDouble(2, location.getLongitude());

            result = statement.executeQuery();
            if (result.next()) {
                locationID = Integer.parseInt(result.getString("locationid"));
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            amazonRDS.close(result, statement);
        }
        return locationID;
    }


    public int addLocation(Location location) throws DaoException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet result = null;
        try {
            con = amazonRDS.getConnection();
            statement = con.prepareStatement(SQL_INSERT_LOCATION);
            statement.setString(1, location.getLocationName());
            statement.setDouble(2, location.getLatitude());
            statement.setDouble(3, location.getLongitude());
            if (statement.executeUpdate() != 0) {
                con.commit();
            } else {
                return -1;
            }
            return getLocationID(location);
        } catch (SQLException e) {
            throw new DaoException(e);
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            amazonRDS.close(result, statement);
        }
    }

}
