package com.alttd.chat.requests;

import java.util.UUID;

public class PrefixRequest extends Request{

    public PrefixRequest(UUID requester, String request) {
        super(requester, request);

        this.requestType = RequestType.PREFIX;
    }

    public PrefixRequest(UUID requester, String request, boolean completed, UUID completedBy, long dateRequested, long dateCompleted) {
        super(requester, request, completed, completedBy, dateRequested, dateCompleted);

        this.requestType = RequestType.NICKNAME;
    }

    @Override
    public boolean processRequest(UUID processor) {
        return false;
    }

}
