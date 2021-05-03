package com.alttd.chat.handlers;

import java.util.UUID;

public class ChatPlayer {

    private UUID uuid;
    private UUID replyTarget;
    private boolean globalChatEnabled;

    public ChatPlayer(UUID uuid) {
        this.uuid = uuid;
        this.replyTarget = null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isGlobalChatEnabled() {
        return globalChatEnabled;
    }

    public UUID getReplyTarget() {
        return replyTarget;
    }

    public void setReplyTarget(UUID uuid) {
        if(!replyTarget.equals(uuid))
            replyTarget = uuid;
    }
}
