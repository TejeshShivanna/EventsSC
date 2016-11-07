package eventssc.dao;

import java.sql.*;

public class DatabaseManager {

	private static final String HOSTNAME = "aag5mhra74ej9f.c5ylyjbogtox.us-west-2.rds.amazonaws.com";
	private static final int PORT = 5432;
	private static final String DATABASENAME = "postgres";
	private static final String USERNAME = "postgres";
	private static final String PASSWORD = "eventssc";

	private static final ThreadLocal<Connection> threadConnection = new ThreadLocal<Connection>();
	
	private static String getConnectionUrl() {
		StringBuffer connectionUrl = new StringBuffer();
		connectionUrl.append("jdbc:postgresql://");
		connectionUrl.append(HOSTNAME + ":" + PORT);
		connectionUrl.append("/" + DATABASENAME + "?");
		connectionUrl.append("user=" + USERNAME);
		connectionUrl.append("&password=" + PASSWORD);
		return connectionUrl.toString();
	}

	public static Connection getConnection() throws DaoException {

		try {
			Connection con = null;
			if (threadConnection.get() == null) {
				Class.forName("org.postgresql.Driver");
				con = DriverManager.getConnection(getConnectionUrl());
				threadConnection.set(con);
			}

			return threadConnection.get();
		}

		catch (SQLException e) {
			throw new DaoException(e);
		} catch (ClassNotFoundException e) {
			throw new DaoException(e);
		}

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
