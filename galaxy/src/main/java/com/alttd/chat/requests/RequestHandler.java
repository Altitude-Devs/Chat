package com.alttd.chat.requests;

import java.util.*;

public class RequestHandler {

    private final HashMap<UUID, Request> requests;

    public RequestHandler() {
        requests = new HashMap<>();
    }

    public void addRequest(Request request) {
        requests.putIfAbsent(request.requester, request);
    }

    public void removeRequest(Request request) {
        requests.remove(request.requester);
    }

    public HashMap<UUID, Request> getRequests() {
        return requests;
    }

}
