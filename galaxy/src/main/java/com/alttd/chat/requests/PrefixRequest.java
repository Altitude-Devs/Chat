package com.alttd.chat.requests;

import java.util.UUID;

public class PrefixRequest extends Request{

    public PrefixRequest(UUID requester, String request) {
        super(requester, request);

        this.requestType = RequestType.PREFIX;
    }

    @Override
    public boolean processRequest(UUID processor) {
        return false;
    }

}
