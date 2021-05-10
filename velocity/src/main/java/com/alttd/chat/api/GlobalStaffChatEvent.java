package com.alttd.chat.api;

import com.velocitypowered.api.command.CommandSource;

public class GlobalStaffChatEvent {
    private final CommandSource sender;
    private final String message;

    public GlobalStaffChatEvent(CommandSource sender, String message) {
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
