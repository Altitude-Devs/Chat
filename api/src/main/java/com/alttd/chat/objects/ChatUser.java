package com.alttd.chat.objects;

import com.alttd.chat.database.Queries;
import com.alttd.chat.util.Utility;

import java.util.LinkedList;
import java.util.UUID;

public class ChatUser {
    private final UUID uuid;
    private final int partyId;
    private boolean toggledPartyChat;
    private boolean forceTp;
    private String displayName;
    private String prefix;
    private String staffPrefix;
    private String prefixAll;
    private boolean toggleGc;
    private UUID replyTarget;
    private long gcCooldown;

    private LinkedList<Mail> mails;
    private LinkedList<UUID> ignoredPlayers;

    public ChatUser(UUID uuid, int partyId, boolean toggled_chat, boolean force_tp, boolean toggle_Gc) {
        this.uuid = uuid;
        this.partyId = partyId;
        this.toggledPartyChat = toggled_chat;
        this.forceTp = force_tp;

        displayName = Queries.getNickname(uuid);
        if (displayName == null) {
            displayName = Utility.getDisplayName(uuid);
        }

        prefix = Utility.getPrefix(uuid, true);
        staffPrefix = Utility.getStaffPrefix(uuid);

        prefixAll = Utility.getPrefix(uuid, false);

        toggleGc = toggle_Gc;
        replyTarget = null;
        gcCooldown = System.currentTimeMillis(); // players can't use gc for 30 seconds after logging in if we use this?
        mails = new LinkedList<>(); // todo load mails
        ignoredPlayers = new LinkedList<>(); // todo load ignoredPlayers
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getPartyId() {
        return partyId;
    }

    public boolean toggledPartyChat() {
        return toggledPartyChat;
    }

    public void togglePartyChat() {
        toggledPartyChat = !toggledPartyChat;
        Queries.setPartyChatState(toggledPartyChat, uuid); //TODO: Async pls - no CompleteableFuture<>!
    }

    public boolean ForceTp() {
        return forceTp;
    }

    public void toggleForceTp() {
        forceTp = !forceTp;
        Queries.setForceTpState(forceTp, uuid); //TODO: Async pls - no CompleteableFuture<>!
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

    public UUID getReplyTarget() {
        return replyTarget;
    }

    public void setReplyTarget(UUID replyTarget) {
        this.replyTarget = replyTarget;
    }

    public LinkedList<Mail> getMails() {
        return mails;
    }

    public void addMail(Mail mail) {
        mails.add(mail);
    }

    public LinkedList<UUID> getIgnoredPlayers() {
        return ignoredPlayers;
    }

    public void addIgnoredPlayers(UUID uuid) {
        ignoredPlayers.add(uuid);
    }

    public long getGcCooldown() {
        return gcCooldown;
    }

    public void setGcCooldown(long time) {
        this.gcCooldown = time;
    }
}
