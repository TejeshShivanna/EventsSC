package eventssc.controller;

import eventssc.dao.DaoException;
import eventssc.database.AmazonRDS;
import eventssc.event.EventBean;
import eventssc.range.Range;
import eventssc.user.UserManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@RestController
public class ConnectionController {

    final static Logger logger = LoggerFactory.getLogger(AmazonRDS.class);

    private AmazonRDS amazonRDS;
    private Range range;
    private EventBean eventBean;

    @Autowired
    public ConnectionController(AmazonRDS amazonRDS, Range range, EventBean eventBean) throws Exception {
        this.amazonRDS = amazonRDS;
        this.range = range;
        this.eventBean = eventBean;
    }

    @RequestMapping("/connection")
    public String connected(@RequestParam(value = "name", defaultValue = "Events@SC") String name) {
        return name;
    }

    @RequestMapping("/usc")
    public JSONObject getUscDetails() throws Exception {
        Connection connection = null;
        JSONObject uscDetails = new JSONObject();
        ResultSet resultSet;

        try {
            String selectUSC = "SELECT * FROM USERDETAILS WHERE FIRSTNAME='USC'";

            connection = amazonRDS.getConnection();
            Statement statement = connection.createStatement();

            resultSet = statement.executeQuery(selectUSC);

            if (resultSet.next()) {
                uscDetails.put("FIRSTNAME", resultSet.getString("FIRSTNAME"));
                uscDetails.put("LASTNAME", resultSet.getString("LASTNAME"));
                uscDetails.put("CONTACTNUMBER", resultSet.getString("CONTACTNUMBER"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage());
            if (connection != null) connection.rollback();
        } finally {
            if (connection != null) connection.close();
        }
        return uscDetails;
    }

    @RequestMapping("/range")
    public String rangeQuery(@RequestParam(value = "latLong", defaultValue = "{\n" +
            "                  \"latitude\" : 34.0230895,\n" +
            "                  \"longitude\" : -118.2870363,\n" +
            "                  \"range\" : 0.2\n" +
            "               }") String latLong) throws DaoException {
        return range.getEventsinRange(latLong);
    }

    @RequestMapping("/all_events")
    public String getAllEvents() throws DaoException {
        return eventBean.getAllEvents();
    }

    @RequestMapping("/create")
    public String createEvent(String creationString) throws DaoException {
        return String.valueOf(eventBean.createEvent(creationString));
    }

    @RequestMapping("/markInterest")
    public String markInterest(String interestStr) throws DaoException{
        return String.valueOf(eventBean.markInterest(interestStr));
    }

    @RequestMapping("/getUser")
    public String getUserName(String userIdStr) throws DaoException{
        return UserManager.getFirstNameFromId(userIdStr, amazonRDS);
    }

    @RequestMapping("/getInterestedEvents")
    public String getInterestedEvents(String userIdStr) throws DaoException{
        return eventBean.getInterestedEvents(userIdStr);
    }

    /*
    Input: UserName and Password
    Returns LOGINID from LOGIN table if credentials are correct. Else returns -1.
     */
    @RequestMapping("/login")
    public int getLogin(String username, String password) throws Exception{
        Connection connection = null;
        ResultSet resultSet;

        try{
            String selectLoginID = "SELECT LOGINID FROM LOGIN WHERE USERNAME = \'" + username + "\'" + "AND PASSWORD = \'" + password + "\'";

            connection = amazonRDS.getConnection();
            Statement statement = connection.createStatement();

            resultSet = statement.executeQuery(selectLoginID);
            if(resultSet.next()){
                String selectUserID = "SELECT USERID FROM USERDETAILS WHERE LOGINID = " + Integer.parseInt(resultSet.getString("LOGINID"));
                resultSet = statement.executeQuery(selectUserID);

                if(resultSet.next()){
                    return Integer.parseInt(resultSet.getString("USERID"));
                }
            }
        }
        catch(Exception ex){
            logger.error(ex.getMessage());
            if(connection!=null) connection.rollback();
        }
        finally {
            if(connection!=null) connection.close();
        }
        return -1;
    }

    @RequestMapping("/registeruser")
    public int addUser(String user) throws Exception{
        Connection connection = null;
        ResultSet resultSet;

        try{
            JSONParser parser = new JSONParser();
            JSONObject userJson = (JSONObject) parser.parse(user);

            String username = (String)userJson.get("username");
            String password = (String)userJson.get("password");
            String firstname = (String)userJson.get("firstname");
            String lastname = (String)userJson.get("lastname");
            String phone = (String)userJson.get("phone");


            String selectLoginID = "SELECT LOGINID FROM LOGIN WHERE USERNAME = \'" + username + "\'";

            connection = amazonRDS.getConnection();
            Statement statement = connection.createStatement();

            resultSet = statement.executeQuery(selectLoginID);
            if(resultSet.next()){
                return -1;
            }
            else{
                String insertUscLogin = "INSERT INTO LOGIN (USERNAME, PASSWORD) VALUES( \'" + username + "\' , \'" + password + "\')";
                statement.executeUpdate(insertUscLogin);

                resultSet = statement.executeQuery(selectLoginID);
                if (resultSet.next()){
                    int uscLoginID = Integer.parseInt(resultSet.getString("LOGINID"));

                    StringBuilder insertUserDetail = new StringBuilder();
                    insertUserDetail.append("INSERT INTO USERDETAILS (FIRSTNAME, LASTNAME, LOGINID, CONTACTNUMBER)");
                    insertUserDetail.append(" VALUES(");

                    insertUserDetail.append("\'" + firstname + "\'" + ",");
                    insertUserDetail.append("\'" + lastname + "\'" + ",");
                    insertUserDetail.append("\'" + uscLoginID + "\'" + ",");
                    insertUserDetail.append("\'" + phone + "\'");
                    insertUserDetail.append(")");

                    statement.executeUpdate(insertUserDetail.toString());

                    String selectUserID = "SELECT USERID FROM USERDETAILS WHERE LOGINID = " + uscLoginID;
                    resultSet = statement.executeQuery(selectUserID);

                    if(resultSet.next()){
                        connection.commit();
                        return Integer.parseInt(resultSet.getString("USERID"));
                    }
                }
            }
        }
        catch(Exception ex){
            logger.error(ex.getMessage());
            if(connection!=null) connection.rollback();
        }
        finally {
            if(connection!=null) connection.close();
        }
        return -1;
    }
    @RequestMapping("/geofence")
    public String getGeoFencingDetails() throws Exception {
        Connection connection = null;
        ResultSet resultSet;
        JSONArray geofenceArray = new JSONArray();

        try {
            String selectUSC = "SELECT E.EVENTNAME, L.LATITUDE, L.LONGITUDE FROM EVENT E INNER JOIN LOCATION L ON E.LOCATIONID=L.LOCATIONID WHERE E.EVENTDATE=((SELECT NOW() AT TIME ZONE 'PST') ::timestamp::date) AND E.ENDTIME>=(SELECT ((SELECT NOW() AT TIME ZONE 'PST') ::timestamp::time))";

            connection = amazonRDS.getConnection();
            Statement statement = connection.createStatement();

            resultSet = statement.executeQuery(selectUSC);

            while (resultSet.next()) {
                JSONObject geofence = new JSONObject();
                geofence.put("eventname", resultSet.getString("EVENTNAME"));
                geofence.put("latitude", resultSet.getString("LATITUDE"));
                geofence.put("longitude", resultSet.getString("LONGITUDE"));
                geofenceArray.add(geofence);
            }
            return geofenceArray.toJSONString();

        } catch (Exception ex) {
            ex.printStackTrace();
            logger.error(ex.getMessage());
            if (connection != null)
                connection.rollback();
        } finally {
            if (connection != null)
                connection.close();
        }
        return null;
    }
}
