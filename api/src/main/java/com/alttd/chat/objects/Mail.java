package com.alttd.chat.objects;

import java.util.UUID;

public class Mail {

    private final UUID uuid;
    private final UUID sender;
    private final long sendTime;
    private long readTime;
    private final String message;

    public Mail(UUID player, UUID sender, long sendTime, long readTime, String message) {
        this.uuid = player;
        this.sender = sender;
        this.sendTime = sendTime;
        this.readTime = readTime;
        this.message = message;
    }

    public Mail(UUID player, UUID sender, String message) {
        this.uuid = player;
        this.sender = sender;
        this.sendTime = System.currentTimeMillis();
        this.readTime = System.currentTimeMillis();
        this.message = message;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getSender() {
        return sender;
    }

    public boolean isUnRead() {
        return getSendTime() != getReadTime();
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
