package com.alttd.chat.data;

import java.util.UUID;

public class Mail {

    private final UUID uuid; // the player
    private final UUID sender; // the sender
    private boolean read;
    private final long sendTime; // any other option for this? does the db store recordcreation and edit time?
    private long readTime; // any other option for this?
    private final String message; // do we want staff to edit this after being send but being unread?

    public Mail(UUID player, UUID sender, Boolean read, long sendTime, long readTime, String message) {
        this.uuid = player;
        this.sender = sender;
        this.read = read;
        this.sendTime = sendTime;
        this.readTime = readTime;
        this.message = message;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getSender() {
        return sender;
    }

    public boolean isUnRead() {
        return read;
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public long getSendTime() {
        return sendTime;
    }

    public long getReadTime() {
        return readTime;
    }

    public void setReadTime(long readTime) {
        this.readTime = readTime;
    }

    public String getMessage() {
        return message;
    }
}
