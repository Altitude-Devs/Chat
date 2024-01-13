package com.alttd.chat.requests;

import org.bukkit.Bukkit;

import java.util.Date;
import java.util.UUID;

public abstract class Request {

    protected UUID requester;
    protected RequestType requestType;
    protected String serverName;
    protected String request;
    protected boolean completed;
    protected UUID completedBy;
    protected long dateRequested;
    protected long dateCompleted;

    Request(UUID requester, String request) {
        this.requester = requester;
        this.request = request;
        this.serverName = Bukkit.getServerName();
        this.dateRequested = new Date().getTime();
        saveRequest();
    }

    Request(UUID requester, String request, boolean completed, UUID completedBy, long dateRequested, long dateCompleted) {
        this.requester = requester;
        this.request = request;
        this.completed = completed;
        this.completedBy = completedBy;
        this.dateRequested = dateRequested;
        this.dateCompleted = dateCompleted;
    }

    public static Request of(UUID requester, RequestType requestType, String request) {
        return switch (requestType) {
            case PREFIX -> new PrefixRequest(requester, request);
            case NICKNAME -> new NickNameRequest(requester, request);
        };
    }

    public static Request load(UUID requester, RequestType requestType, String request, boolean completed, UUID completedBy, long dateRequested, long dateCompleted) {
        return switch (requestType) {
            case PREFIX -> new PrefixRequest(requester, request, completed, completedBy, dateRequested, dateCompleted);
            case NICKNAME -> new NickNameRequest(requester, request, completed, completedBy, dateRequested, dateCompleted);
        };
    }

    public boolean processRequest(UUID completedBy) {
        completeRequest(completedBy);
        return true;
    }

    public boolean isCompleted() {
        return completed;
    }

    void completeRequest(UUID completedBy) {
        this.completed = true;
        this.completedBy = completedBy;
        this.dateCompleted = new Date().getTime();
        saveRequest();
    }

    public void saveRequest() {
        // upsert into database
    }
}
