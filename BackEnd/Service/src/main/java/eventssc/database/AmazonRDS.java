package eventssc.database;

import eventssc.dao.DaoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

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

    public String getConnectionUrl(){
        StringBuffer connectionUrl = new StringBuffer();
        connectionUrl.append("jdbc:postgresql://");
        connectionUrl.append(hostName + ":" + port);
        connectionUrl.append("/" + databaseName + "?");
        connectionUrl.append("user=" + userName);
        connectionUrl.append("&password=" + password);
        return connectionUrl.toString();
    }

    public Connection getConnection() throws Exception{
        try {
            Connection connection = DriverManager.getConnection(getConnectionUrl());
            connection.setAutoCommit(false);
            return connection;
        }
        catch (Exception ex){
            ex.printStackTrace();
            logger.error(ex.getMessage());
        }
        return null;
    }

    public static void close(ResultSet resultSet, Statement statement) throws DaoException {

        try {

            if (resultSet != null) {
                resultSet.close();
            }

            if (statement != null) {
                statement.close();
            }

        } catch (SQLException e) {
            throw new DaoException(e);
        }

    }
}