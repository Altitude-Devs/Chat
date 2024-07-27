package com.alttd.chat.database;


import com.alttd.chat.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private static Connection connection;

    /**
     * Sets information for the database and opens the connection.
     */
    public DatabaseConnection() {
        instance = this;

        try {
            instance.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Opens the connection if it's not already open.
     * @throws SQLException If it can't create the connection.
     */
    public void openConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return;
        }

        synchronized (this) {
            if (connection != null && !connection.isClosed()) {
                return;
            }
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }

            connection = DriverManager.getConnection(
                    "jdbc:mysql://" + Config.IP + ":" + Config.PORT + "/" + Config.DATABASE + "?autoReconnect=true"+
                    "&useSSL=false",
                    Config.USERNAME, Config.PASSWORD);
        }
    }

    /**
     * Returns the connection for the database
     * @return Returns the connection.
     */
    public static Connection getConnection() {
        try {
            instance.openConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return connection;
    }

    /**
     * Creates a transactional database connection.
     *
     * @return A {@code Connection} object representing the transactional database connection.
     * @throws SQLException If there is an error creating the database connection.
     */
    public static Connection createTransactionConnection() throws SQLException {
        connection = DriverManager.getConnection(
                "jdbc:mysql://" + Config.IP + ":" + Config.PORT + "/" + Config.DATABASE + "?autoReconnect=true"+
                        "&useSSL=false",
                Config.USERNAME, Config.PASSWORD);
        connection.setAutoCommit(false);
        return connection;
    }

    /**
     * Sets the connection for this instance
     */
    public static boolean initialize() {
        instance = new DatabaseConnection();
        return connection != null;
    }

}