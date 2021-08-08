package com.alttd.chat.objects;

import com.alttd.chat.util.Utility;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class PartyUser {

    protected UUID uuid;
    protected Component displayName;
    protected String playerName;

    public PartyUser(UUID uuid, String displayName, String playerName) {
        this(uuid, Utility.applyColor(displayName), playerName);
    }

    public PartyUser(UUID uuid, Component displayName, String playerName) {
        this.uuid = uuid;
        this.displayName = displayName;
        this.playerName = playerName;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public String getPlayerName() {
        return playerName;
    }

}
