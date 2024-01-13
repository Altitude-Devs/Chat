package com.alttd.chat.requests;

import java.util.UUID;

public class NickNameRequest extends Request {

    public NickNameRequest(UUID requester, String request) {
        super(requester, request);

        this.requestType = RequestType.NICKNAME;
    }

    public NickNameRequest(UUID requester, String request, boolean completed, UUID completedBy, long dateRequested, long dateCompleted) {
        super(requester, request, completed, completedBy, dateRequested, dateCompleted);

        this.requestType = RequestType.NICKNAME;
    }
    @Override
    public boolean processRequest(UUID processor) {
        return false;
    }

}
