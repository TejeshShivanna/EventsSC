package database;

import crawl.Crawl;
import location.UscLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import table.Event;
import table.Location;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class AmazonRDS {

    private String hostName;

    private String databaseName;

    private String userName;

    private String password;

    private int port;

    final static Logger logger = LoggerFactory.getLogger(AmazonRDS.class);

    public AmazonRDS(String hostName, String databaseName, String userName, String password, int port){
        this.hostName = hostName;
        this.databaseName = databaseName;
        this.userName = userName;
        this.password = password;
        this.port = port;
    }

    private String getConnectionUrl(){
        StringBuffer connectionUrl = new StringBuffer();
        connectionUrl.append("jdbc:postgresql://");
        connectionUrl.append(hostName + ":" + port);
        connectionUrl.append("/" + databaseName + "?");
        connectionUrl.append("user=" + userName);
        connectionUrl.append("&password=" + password);
        return connectionUrl.toString();
    }

    private Connection getConnection() throws Exception{
        try {
            Connection connection = DriverManager.getConnection(getConnectionUrl());
            connection.setAutoCommit(false);
            return connection;
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
        return null;
    }

    public HashMap<String, Integer> getLoginAndUserID(String userName) throws Exception{
        Connection connection = null;
        HashMap<String, Integer> loginAndUserID = new HashMap();
        ResultSet resultSet;

        try{
            int uscLoginID = -1;
            int uscUserID = -1;
            String selectLoginID = "SELECT LOGINID FROM LOGIN WHERE USERNAME = \'" + userName + "\'";
            String insertUscLogin = "INSERT INTO LOGIN (USERNAME, PASSWORD) VALUES( \'" + userName + "\', 'password')";

            connection = getConnection();
            Statement statement = connection.createStatement();

            // get LOGINID
            resultSet = statement.executeQuery(selectLoginID);
            if(!resultSet.next()){
                statement.executeUpdate(insertUscLogin);
            }
            resultSet = statement.executeQuery(selectLoginID);
            while (resultSet.next()){
                uscLoginID = Integer.parseInt(resultSet.getString("LOGINID"));
            }

            // get USERID
            String selectUserID = "SELECT USERID FROM USERDETAILS WHERE LOGINID = " + uscLoginID;
            resultSet = statement.executeQuery(selectUserID);
            if (!resultSet.next()) {
                String insertUserDetail = "INSERT INTO USERDETAILS (FIRSTNAME, LASTNAME, LOGINID, CONTACTNUMBER) " +
                        "VALUES('USC', 'Viterbi', " + uscLoginID + ", '213-740-4530')";
                statement.executeUpdate(insertUserDetail);
            }
            resultSet = statement.executeQuery(selectUserID);
            while (resultSet.next()){
                uscUserID = Integer.parseInt(resultSet.getString("USERID"));
            }

            loginAndUserID.put("LOGINID", uscLoginID);
            loginAndUserID.put("USERID", uscUserID);
        }
        catch(Exception ex){
            logger.error(ex.getMessage());
            if(connection!=null) connection.rollback();
        }
        finally {
            if(connection!=null) connection.close();
        }
        return loginAndUserID;
    }

    public void writeToLocationTable() throws Exception{
        Connection connection = null;
        try{
            connection = getConnection();
            Statement statement = connection.createStatement();

            Crawl crawl = new Crawl();
            List<UscLocation> uscLocationList = crawl.getUscLocationsInfo();
            Iterator<UscLocation> uscLocationIterator = uscLocationList.iterator();
            UscLocation uscLocation;
            Location location = new Location();
            while (uscLocationIterator.hasNext()){
                uscLocation = uscLocationIterator.next();
                location.setLocationCoOrdinates(uscLocation.getAddress());
                StringBuffer insertQuery = new StringBuffer();
                insertQuery.append("INSERT INTO LOCATION (ADDRESS, LATITUDE, LONGITUDE) VALUES(");
                insertQuery.append("'" + location.getAddress() + "'");
                insertQuery.append("," + location.getLatitude());
                insertQuery.append("," + location.getLongitude());
                insertQuery.append(")");
                statement.executeUpdate(insertQuery.toString());
            }
            connection.commit();
            statement.close();
            connection.close();
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
            if(connection!=null) connection.rollback();
        }
        finally {
            if(connection!=null) connection.close();
        }
    }

    public void writeToEventTable() throws Exception{
        Connection connection = null;
        try{
            HashMap<String, Integer> loginAndUserID = getLoginAndUserID("usc");
            Crawl crawlUSC = new Crawl();
            List<Event> eventList = crawlUSC.getEventsInfo();
            Iterator<Event> eventIterator = eventList.iterator();
            Location location = new Location();
            Event event;

            connection = getConnection();
            Statement statement = connection.createStatement();
            StringBuffer insertQuery = new StringBuffer();
            while(eventIterator.hasNext()){
                event = eventIterator.next();
                location.setLocationCoOrdinates(event.getAddress());
                int locationID = getLocationID(location);
                insertQuery.append("INSERT INTO EVENT (EVENTNAME, EVENTDESCRIPTION, EVENTDATE, STARTTIME, ENDTIME, LOCATIONID, CREATOR) VALUES(");
                insertQuery.append("'" + event.getName() + "'" + ",");
                insertQuery.append("'" + event.getDescription() + "'" + ",");
                insertQuery.append("'" + formatDate(event.getDate()) + "'" + ","); // find how to insert date and time
                insertQuery.append("'" + event.getStartTime() + "'" + ",");
                insertQuery.append("'" + event.getEndTime() + "'" + ",");
                insertQuery.append("'" + locationID + "'" + ","); // add location ID here
                insertQuery.append(loginAndUserID.get("USERID"));
                insertQuery.append(")");
                statement.executeUpdate(insertQuery.toString());
                insertQuery.setLength(0);
            }
            connection.commit();
            statement.close();
            connection.close();
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
            if(connection!=null) connection.rollback();
        }
        finally {
            if(connection!=null) connection.close();
        }
    }

    public String formatDate(String stringDate) throws Exception{
        DateFormat df = new SimpleDateFormat("EEE, dd MMM, yyyy", Locale.US);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MMM-dd", Locale.ENGLISH);

        String result =  formatter.format(df.parse(stringDate));
        return result;
    }

    public int getLocationID(Location location) throws Exception{
        Connection connection = null;
        int locationID = -1;
        try {
            connection = getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet;

            String getLocationIDQuery = "SELECT locationid FROM LOCATION WHERE latitude=" + location.getLatitude() + " AND " + "longitude=" + location.getLongitude();
            resultSet = statement.executeQuery(getLocationIDQuery);
            if(resultSet.next()) {
                locationID = Integer.parseInt(resultSet.getString("locationid"));
            }
            else{
                StringBuffer insertQuery = new StringBuffer();
                insertQuery.append("INSERT INTO LOCATION (ADDRESS, LATITUDE, LONGITUDE) VALUES(");
                insertQuery.append("'" + location.getAddress() + "'");
                insertQuery.append("," + location.getLatitude());
                insertQuery.append("," + location.getLongitude());
                insertQuery.append(")");
                statement.executeUpdate(insertQuery.toString());
                getLocationIDQuery = "SELECT locationid FROM LOCATION WHERE latitude=" + location.getLatitude() + " AND " + "longitude=" + location.getLongitude();
                resultSet = statement.executeQuery(getLocationIDQuery);
                if(resultSet.next()) {
                    locationID = Integer.parseInt(resultSet.getString("locationid"));
                }
            }
            connection.commit();
            statement.close();
            connection.close();
        }
        catch(Exception ex){
            logger.error(ex.getMessage());
            if(connection!=null) connection.rollback();
        }
        finally {
            if(connection!=null) connection.close();
        }
        return locationID;
    }
}