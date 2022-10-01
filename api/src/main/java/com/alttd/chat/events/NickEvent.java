package com.alttd.chat.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class NickEvent extends Event{

    String senderName;
    String targetName;
    String nickName;
    NickEventType nickEventType;

    public NickEvent(String senderName, String targetName, String nickName, NickEventType nickEventType) {
        this.senderName = senderName;
        this.targetName = targetName;
        this.nickName = nickName;
        this.nickEventType = nickEventType;
    }

    public String getSenderName() {
        return this.senderName;
    }

    public String getTargetName() {
        return this.targetName;
    }

    public String getNickName() {
        return this.nickName;
    }

    public NickEventType getNickEventType() {
        return this.nickEventType;
    }

    private static final HandlerList HANDLERS = new HandlerList();

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public enum NickEventType {
        ACCEPTED,
        DENIED,
        SET,
        RESET
    }

}