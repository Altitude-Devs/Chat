package com.alttd.velocitychat.api;

import com.velocitypowered.api.command.CommandSource;

public class GlobalAdminChatEvent {
    private final CommandSource sender;
    private final String message;

    public GlobalAdminChatEvent(CommandSource sender, String message) {
        this.sender = sender;
        this.message = message;
    }

    public CommandSource getSender() {
        return sender;
    }

    public String getMessage() {
        return message;
    }
}
