package eventssc.user;

import eventssc.dao.DaoException;
import eventssc.dao.UserDao;
import eventssc.database.AmazonRDS;

/**
 * Created by Sharath_GM on 11/27/16.
 */
public class UserManager {

    public static String getFirstNameFromId(String userIdStr, AmazonRDS amazonRDS) throws DaoException {
        String userName = null;
        if (userIdStr != null) {
            userName = UserDao.getFirstNameFromId(Integer.parseInt(userIdStr), amazonRDS);
        }
        if (userName == null) {
            return "";
        }
        return userName;
    }

}
