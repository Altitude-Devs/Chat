package com.alttd.chat.objects;

import com.alttd.chat.database.Queries;
import com.alttd.chat.util.Utility;
import net.kyori.adventure.text.Component;

import java.util.LinkedList;
import java.util.UUID;

public class ChatUser {
    private final UUID uuid; // player uuid
    private final int partyId; // the party they are in
    private boolean toggledPartyChat; // should chat messages instantly go to party chat when added, idk if this should be saved
    private String name; // the nickname, doesn't need to be saved with the chatuser object, could be saved but we can get it from the nicknamesview
    private Component displayName; // the nickname, doesn't need to be saved with the chatuser object, could be saved but we can get it from the nicknamesview
    private Component prefix; // doesn't need saving, we get this from luckperms
    private Component staffPrefix; // doesn't need saving, we get this from luckperms
    private Component prefixAll; // doesn't need saving, we get this from luckperms
    private boolean toggleGc; // should be saved, this toggles if the player can see and use global chat
    private String replyTarget; // reply target for use in /msg i don't mind setting this to null on login, feedback?
    private long gcCooldown; // the time when they last used gc, is used for the cooldown, i wouldn't save this, but setting this to the login time means they can't use gc for 30 seconds after logging in

    private LinkedList<Mail> mails; // mails aren't finalized yet, so for now a table sender, reciever, sendtime, readtime(if emtpy mail isn't read yet?, could also do a byte to control this), the actual message
    private LinkedList<UUID> ignoredPlayers; // a list of UUID, a new table non unique, where one is is the player select * from ignores where ignoredby = thisplayer? where the result is the uuid of the player ignored by this player?
    private LinkedList<UUID> ignoredBy; // a list of UUID, same table as above but select * from ignores where ignored = thisplayer? result should be the other user that ignored this player?

    public ChatUser(UUID uuid, int partyId, boolean toggledChat, boolean toggleGc) {
        this.uuid = uuid;
        this.partyId = partyId;
        this.toggledPartyChat = toggledChat;

        name = Queries.getNickname(uuid);
        if (name == null) {
            name = Utility.getDisplayName(uuid);
        }
        setDisplayName(name);

        prefix = Utility.getPrefix(uuid, true);
        staffPrefix = Utility.getStaffPrefix(uuid);

        prefixAll = Utility.getPrefix(uuid, false);

        this.toggleGc = toggleGc;
        replyTarget = null;
        gcCooldown = System.currentTimeMillis(); // players can't use gc for 30 seconds after logging in if we use this?
        mails = new LinkedList<>(); // todo load mails
        ignoredPlayers = Queries.getIgnoredUsers(uuid);
        ignoredBy = new LinkedList<>(); // todo load ignoredPlayers
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

    public Component getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = Utility.applyColor(displayName);
    }

    public Component getPrefix() {
        return prefix;
    }

    public Component getStaffPrefix() {
        return staffPrefix;
    }

    public Component getPrefixAll() {
        return prefixAll;
    }

    public void toggleGc() {
        toggleGc = !toggleGc;
    }

    public boolean isGcOn() {
        return toggleGc;
    }

    public String getReplyTarget() {
        return replyTarget;
    }

    public void setReplyTarget(String replyTarget) {
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

    public LinkedList<UUID> getIgnoredBy() {
        return ignoredBy;
    }

    public void addIgnoredBy(UUID uuid) {
        ignoredBy.add(uuid);
    }

    public long getGcCooldown() {
        return gcCooldown;
    }

    public void setGcCooldown(long time) {
        this.gcCooldown = time;
    }
}
