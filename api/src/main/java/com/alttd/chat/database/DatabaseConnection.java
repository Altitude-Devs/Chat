package com.alttd.chat.database;


import com.alttd.chat.config.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;

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
                Class.forName("com.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            connection = DriverManager.getConnection(
                    "jdbc:" + Config.DRIVER + "://" + Config.IP + ":" + Config.PORT + "/" + Config.DATABASE + "?autoReconnect=true",
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
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return instance.connection;
    }

    /**
     * Sets the connection for this instance
     */
    public boolean initialize() {
        instance = new DatabaseConnection();
        return connection != null;
    }

}