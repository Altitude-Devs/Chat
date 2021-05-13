package com.alttd.chat.api;


import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.proxy.Player;

import java.util.Objects;

public class PrivateMessageEvent implements ResultedEvent<ResultedEvent.GenericResult>  {
    private final CommandSource sender;
    private final Player recipient;
    private final String message;

    private GenericResult result = GenericResult.allowed(); // Allowed by default

    public PrivateMessageEvent(CommandSource sender, Player recipient, String message) {
        this.sender = sender;
        this.recipient = recipient;
        this.message = message;
    }

    public CommandSource getSender() {
        return sender;
    }

    public Player getRecipient() {
        return recipient;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public GenericResult getResult() {
        return result;
    }

    @Override
    public void setResult(GenericResult result) {
        this.result = Objects.requireNonNull(result);
    }
}
