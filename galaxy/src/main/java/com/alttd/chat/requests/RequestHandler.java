package com.alttd.chat.requests;

import com.alttd.chat.config.Config;
import com.alttd.chat.database.DatabaseConnection;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class RequestHandler {

    private final List<Request> requests;

    public RequestHandler() {
        requests = new ArrayList<>();
    }

    public boolean addRequest(Request request) {
        return requests.add(request);
    }

    public boolean removeRequest(Request request) {
        return requests.remove(request);
    }

    public List<Request> getRequests() {
        return requests;
    }

    public void loadRequests() {
        long time = new Date().getTime() - Config.NICK_WAIT_TIME;
        // Load all requests that have not been completed yet
        String query = "SELECT * FROM requests WHERE completed=false and (datechanged = 0 or datechanged > " + time + ")";

        try {
            Connection connection = DatabaseConnection.getConnection();
            ResultSet resultSet = connection.prepareStatement(query).executeQuery();

            while (resultSet.next()) {
                UUID requester = UUID.fromString(resultSet.getString("requester"));
                RequestType requestType = RequestType.valueOf(resultSet.getString("requesttype"));
                String requestString = resultSet.getString("request");
                boolean completed = resultSet.getBoolean("completed");
                UUID completedby = UUID.fromString(resultSet.getString("completedby"));
                long dateRequested = resultSet.getLong("daterequested");
                long dateCompleted = resultSet.getLong("datecompleted");

                Request request = Request.load(requester, requestType, requestString, completed, completedby, dateRequested, dateCompleted);
                addRequest(request);
            }

        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

}
