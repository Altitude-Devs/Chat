package com.alttd.chat.requests;

import org.bukkit.Bukkit;

import java.util.UUID;

public abstract class Request {

    protected UUID requester;
    protected RequestType requestType;
    protected String serverName;
    protected String request;
    protected boolean completed;
    protected boolean acceptedBy;

    Request(UUID requester, String request) {
        this.requester = requester;
        this.request = request;
        this.serverName = Bukkit.getServerName();
    }

    public static Request of(UUID requester, RequestType requestType, String request) {
        return switch (requestType) {
            case PREFIX -> new PrefixRequest(requester, request);
            case NICKNAME -> new NickNameRequest(requester, request);
        };
    }

    public abstract boolean processRequest(UUID processor);

    public boolean isCompleted() {
        return completed;
    }

}
