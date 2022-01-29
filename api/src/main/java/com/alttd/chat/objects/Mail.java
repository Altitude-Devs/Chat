package com.alttd.chat.objects;

import com.alttd.chat.database.Queries;

import java.util.UUID;

public class Mail {

    private final int id;
    private final UUID uuid;
    private final UUID sender;
    private final long sendTime;
    private long readTime;
    private final String message;

    public Mail(int id, UUID player, UUID sender, long sendTime, long readTime, String message) {
        this.id = id;
        this.uuid = player;
        this.sender = sender;
        this.sendTime = sendTime;
        this.readTime = readTime;
        this.message = message;
    }

    public Mail(UUID player, UUID sender, String message) {
        this.uuid = player;
        this.sender = sender;
        long time = System.currentTimeMillis();
        this.sendTime = time;
        this.readTime = time;
        this.message = message;
        this.id = Queries.insertMail(this);
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public UUID getSender() {
        return sender;
    }

    public boolean isUnRead() {
        return getSendTime() == getReadTime();
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
