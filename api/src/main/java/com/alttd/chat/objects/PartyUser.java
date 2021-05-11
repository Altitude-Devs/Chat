package com.alttd.chat.objects;

import com.alttd.chat.database.Queries;

import java.util.UUID;

public class PartyUser {
    private final UUID uuid;
    private final int partyId;
    private boolean toggledChat;
    private boolean forceTp;

    public PartyUser(UUID uuid, int partyId, boolean toggled_chat, boolean force_tp) {
        this.uuid = uuid;
        this.partyId = partyId;
        this.toggledChat = toggled_chat;
        this.forceTp = force_tp;
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getPartyId() {
        return partyId;
    }

    public boolean toggledChat() {
        return toggledChat;
    }

    public void toggleChat() {
        toggledChat = !toggledChat;
        Queries.setChatState(toggledChat, uuid); //TODO: Async pls
    }

    public boolean ForceTp() {
        return forceTp;
    }

    public void toggleForceTp() {
        forceTp = !forceTp;
        Queries.setForceTpState(forceTp, uuid); //TODO: Async pls
    }
}
