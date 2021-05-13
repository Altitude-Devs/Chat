package com.alttd.velocitychat.objects;

import java.util.UUID;

public class ChatPlayer {
    // todo merge partyuser here, or make party user extend this?
    // todo gctoggle?

    private UUID uuid;
    private UUID replyTarget;
    private boolean globalChatEnabled; // this vs permission?

    public ChatPlayer(UUID p) {
        uuid = p;
        replyTarget = null;
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
        if (!replyTarget.equals(uuid))
            replyTarget = uuid;
    }
}
