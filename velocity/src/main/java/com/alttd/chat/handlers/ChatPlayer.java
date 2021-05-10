package com.alttd.chat.handlers;

import com.velocitypowered.api.proxy.Player;

import java.util.UUID;

public class ChatPlayer {

    private UUID uuid;
    private Player player;
    private UUID replyTarget;
    private boolean globalChatEnabled;

    public ChatPlayer(Player p) {
        player = p;
        uuid = p.getUniqueId();
        replyTarget = null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Player getPlayer() {
        return player;
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
