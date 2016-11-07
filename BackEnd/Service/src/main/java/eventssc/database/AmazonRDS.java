package eventssc.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.Connection;
import java.sql.DriverManager;

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
            Class.forName("org.postgresql.Driver");
            Connection connection = DriverManager.getConnection(getConnectionUrl());
            connection.setAutoCommit(false);
            return connection;
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
        return null;
    }
}