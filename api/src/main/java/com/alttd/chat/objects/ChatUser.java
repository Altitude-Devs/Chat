package com.alttd.chat.objects;

import com.alttd.chat.database.Queries;

import java.util.UUID;

public class ChatUser {
    private final UUID uuid;
    private final int partyId;
    private boolean toggledChat;
    private boolean forceTp;
    private String displayName;
    private String prefix;
    private String staffPrefix;
    private String prefixAll;
    private boolean toggleGc;

    public ChatUser(UUID uuid, int partyId, boolean toggled_chat, boolean force_tp) {
        this.uuid = uuid;
        this.partyId = partyId;
        this.toggledChat = toggled_chat;
        this.forceTp = force_tp;

        //TODO Get the user somehow and use that to check their prefixes
        displayName = Queries.getNickname(uuid);
        if (displayName == null) {
            //TODO displayName = player.getName() or something
        }

        //TODO Get the user somehow and use that to check the toggleGc permission
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getStaffPrefix() {
        return staffPrefix;
    }

    public void setStaffPrefix(String staffPrefix) {
        this.staffPrefix = staffPrefix;
    }

    public String getPrefixAll() {
        return prefixAll;
    }

    public void setPrefixAll(String prefixAll) {
        this.prefixAll = prefixAll;
    }

    public void toggleGc() {
        toggleGc = !toggleGc;
    }

    public boolean isGcOn() {
        return toggleGc;
    }
}
