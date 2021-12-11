package com.alttd.velocitychat.events;

import com.velocitypowered.api.command.CommandSource;

public class GlobalAdminChatEvent {
    private final CommandSource sender;
    private final String uuid;
    private final String message;

    public GlobalAdminChatEvent(CommandSource sender, String uuid, String message) {
        this.sender = sender;
        this.uuid = uuid;
        this.message = message;
    }

    public CommandSource getSender() {
        return sender;
    }

    public String getUuid() {
        return uuid;
    }

    public String getMessage() {
        return message;
    }
}
