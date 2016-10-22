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
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class AmazonRDS {

    private String hostName;

    private String databaseName;

    private String userName;

    private String password;

    private int port;

    Connection connection = null;

    Statement statement = null;

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
        HashMap<String, Integer> loginAndUserID = new HashMap();
        ResultSet resultSet;
        try{
            int uscLoginID = -1;
            int uscUserID = -1;
            String selectLoginID = "SELECT LOGINID FROM LOGIN WHERE USERNAME = \'" + userName + "\'";
            String insertUscLogin = "INSERT INTO LOGIN (USERNAME, PASSWORD) VALUES( \'" + userName + "\', 'password')";

            connection = getConnection();
            statement = connection.createStatement();

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
        try{
            connection = getConnection();
            statement = connection.createStatement();

            Crawl crawl = new Crawl();
            List<UscLocation> uscLocationList = crawl.getUscLocationsInfo();
            Iterator<UscLocation> uscLocationIterator = uscLocationList.iterator();
            UscLocation uscLocation;
            Location location = new Location();
            while (uscLocationIterator.hasNext()){
                uscLocation = uscLocationIterator.next();
                location.getLocationCoOrdinates(uscLocation.getAddress());
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
        try{
            HashMap<String, Integer> loginAndUserID = getLoginAndUserID("usc");
            Crawl crawlUSC = new Crawl();
            List<Event> eventList = crawlUSC.getEventsInfo();
            Iterator<Event> eventIterator = eventList.iterator();
            Location location = new Location();
            Event event;
            while(eventIterator.hasNext()){
                event = eventIterator.next();
                location.getLocationCoOrdinates(event.getAddress());

            }
        }
        catch(SQLException ex){
            logger.error(ex.getMessage());
        }
        finally {
            connection.close();
        }
    }
}