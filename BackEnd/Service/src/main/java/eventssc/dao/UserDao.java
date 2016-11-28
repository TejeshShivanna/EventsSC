package eventssc.dao;

import eventssc.database.AmazonRDS;

import java.sql.*;

/**
 * Created by Sharath_GM on 11/27/16.
 */
public class UserDao {

    private static final String SQL_GET_USER_ID = "SELECT firstname FROM Userdetails where userid =?";

    public static String getFirstNameFromId(int userId, AmazonRDS amazonRDS) throws DaoException {
        Connection con;
        PreparedStatement statement = null;
        ResultSet result = null;

        String userName = null;

        try {
            con = amazonRDS.getConnection();
            statement = con.prepareStatement(SQL_GET_USER_ID);
            statement.setLong(1, userId);
            result = statement.executeQuery();

            if (result.next()) {
                return result.getString("firstname");
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        } catch (Exception e) {
            throw new DaoException(e);
        } finally {
            amazonRDS.close(result, statement);
        }

        return userName;
    }

}
