package com.alttd.chat.objects;

import com.alttd.chat.util.Utility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class Nick {
    private final UUID uuid;
    private String currentNick;
    private String currentNickNoColor;
    private long lastChangedDate;
    private String newNick;
    private String newNickNoColor;
    private long requestedDate;
    private boolean hasRequest;

    public Nick(UUID uuid, String currentNick, long lastChangedDate) {
        this.uuid = uuid;
        this.currentNick = currentNick;
        currentNickNoColor = currentNick == null ? null : Utility.removeAllColors(currentNick);
        this.lastChangedDate = lastChangedDate;
        newNick = null;
        newNickNoColor = null;
        requestedDate = 0;
        hasRequest = false;
    }

    public Nick(UUID uuid, String currentNick, long lastChangedDate, String newNick, long requestedDate) {
        this.uuid = uuid;
        this.currentNick = currentNick;
        currentNickNoColor = currentNick == null ? null : Utility.removeAllColors(currentNick);
        this.lastChangedDate = lastChangedDate;
        this.newNick = newNick;
        newNickNoColor = newNick == null ? null : Utility.removeAllColors(newNick);
        this.requestedDate = requestedDate;
        hasRequest = newNick != null;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getCurrentNickNoColor() {
        return currentNickNoColor;
    }

    public String getCurrentNick() {
        return currentNick;
    }

    public void setCurrentNick(String currentNick) {
        this.currentNick = currentNick;
        currentNickNoColor = currentNick == null ? null : Utility.removeAllColors(currentNick);
    }

    public long getLastChangedDate(){
        return lastChangedDate;
    }

    public String getLastChangedDateFormatted() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(lastChangedDate));
    }

    public void setLastChangedDate(long lastChangedDate) {
        this.lastChangedDate = lastChangedDate;
    }

    public String getNewNickNoColor() {
        return newNickNoColor;
    }

    public String getNewNick() {
        return newNick;
    }

    public void setNewNick(String newNick) {
        this.newNick = newNick;
        newNickNoColor = newNick == null ? null : Utility.removeAllColors(newNick);
        hasRequest = newNick != null;
    }

    public long getRequestedDate(){
        return requestedDate;
    }

    public String getRequestedDateFormatted() {
        return new SimpleDateFormat("yyyy-MM-dd").format(new Date(requestedDate));
    }

    public void setRequestedDate(long requestedDate) {
        this.requestedDate = requestedDate;
    }

    public boolean hasRequest() {
        return hasRequest;
    }
}
