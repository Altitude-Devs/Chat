package com.alttd.chat.database;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DatabaseConnection instance;
    private Connection connection;
    private final String drivers, ip, port, database, username, password;

    /**
     * Sets information for the database and opens the connection.
     */
    public DatabaseConnection() {

        /*this.drivers = Config.drivers;
        this.ip = Config.ip;
        this.port = Config.port;
        this.database = Config.database;
        this.username = Config.username;
        this.password = Config.password;*/
        // temp to make compile, remove when config is added
        this.drivers = this.ip = this.port = this.database = this.username = this.password = "";
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
                    "jdbc:" + drivers + "://" + ip + ":" + port + "/" + database + "?autoReconnect=true", username,
                    password);
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