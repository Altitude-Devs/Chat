package com.alttd.chat.objects;

import com.alttd.chat.database.Queries;
import com.alttd.chat.util.Utility;

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
    private UUID replytarget;

    public ChatUser(UUID uuid, int partyId, boolean toggled_chat, boolean force_tp, boolean toggle_Gc) {
        this.uuid = uuid;
        this.partyId = partyId;
        this.toggledChat = toggled_chat;
        this.forceTp = force_tp;

        displayName = Queries.getNickname(uuid);
        if (displayName == null) {
            displayName = Utility.getDisplayName(uuid);
        }

        prefix = Utility.getPrefix(uuid, true);
        staffPrefix = Utility.getStaffPrefix(uuid);

        prefixAll = prefix + staffPrefix; //TODO test what this does cus I barely understand lp api
        // a boolean is lighter then a permission check, it's what I'd suggest doing here
        toggleGc = toggle_Gc;//Utility.checkPermission(uuid, "chat.gc"); //TODO put the actual permission here, I don't know what it is...
        replytarget = null;
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

    public UUID getReplytarget() {
        return replytarget;
    }

    public void setReplytarget(UUID replytarget) {
        this.replytarget = replytarget;
    }
}
