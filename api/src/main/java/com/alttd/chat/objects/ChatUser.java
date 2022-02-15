package com.alttd.chat.objects;

import com.alttd.chat.database.Queries;
import com.alttd.chat.objects.channels.Channel;
import com.alttd.chat.util.Utility;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChatUser {
    private final UUID uuid; // player uuid
    private int partyId; // the party they are in
    private Channel toggledChannel;
    private String name; // the nickname, doesn't need to be saved with the chatuser object, could be saved but we can get it from the nicknamesview
    private Component displayName; // the nickname, doesn't need to be saved with the chatuser object, could be saved but we can get it from the nicknamesview
//    private Component prefix; // doesn't need saving, we get this from luckperms
//    private Component staffPrefix; // doesn't need saving, we get this from luckperms
//    private Component prefixAll; // doesn't need saving, we get this from luckperms
    //private boolean toggleGc; // should be saved, this toggles if the player can see and use global chat
    private String replyTarget; // reply target for use in /msg i don't mind setting this to null on login, feedback?
    private long gcCooldown; // the time when they last used gc, is used for the cooldown, i wouldn't save this, but setting this to the login time means they can't use gc for 30 seconds after logging in
    private boolean spy;
    private List<Mail> mails; // mails aren't finalized yet, so for now a table sender, reciever, sendtime, readtime(if emtpy mail isn't read yet?, could also do a byte to control this), the actual message
    private List<UUID> ignoredPlayers; // a list of UUID, a new table non unique, where one is is the player select * from ignores where ignoredby = thisplayer? where the result is the uuid of the player ignored by this player?
    private List<UUID> ignoredBy; // a list of UUID, same table as above but select * from ignores where ignored = thisplayer? result should be the other user that ignored this player?
    private boolean isMuted;

    public ChatUser(UUID uuid, int partyId, Channel toggledChannel) {
        this.uuid = uuid;
        this.partyId = partyId;
        this.toggledChannel = toggledChannel;

        name = Queries.getDisplayName(uuid);
        if (name == null) {
            name = Utility.getDisplayName(uuid, "");
        }
        setDisplayName(name);

//        prefix = Utility.getPrefix(uuid, true); // TODO we need to update this, so cache and update when needed or always request it?
//        staffPrefix = Utility.getStaffPrefix(uuid);
//
//        prefixAll = Utility.getPrefix(uuid, false);

        replyTarget = null;
        gcCooldown = System.currentTimeMillis(); // players can't use gc for 30 seconds after logging in if we use this?
        mails = Queries.getMails(uuid);
        ignoredPlayers = Queries.getIgnoredUsers(uuid);
        ignoredBy = new ArrayList<>(); // todo load ignoredPlayers
        spy = true;
        isMuted = false; // TODO load from db
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getPartyId() {
        return partyId;
    }

    public void setPartyId(int partyId) {
        this.partyId = partyId;
    }

    public Channel getToggledChannel() {
        return toggledChannel;
    }

    public void setToggledChannel(Channel channel) {
        toggledChannel = channel;
        Queries.setToggledChannel(toggledChannel, uuid); //TODO: Async pls - no CompleteableFuture<>!
    }

    public Component getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = Utility.applyColor(displayName);
    }

    public Component getPrefix() {
        //return prefix;
        return Utility.getPrefix(uuid, true); // No longer cache this data
    }

    public Component getStaffPrefix() {
        //return staffPrefix;
        return Utility.getStaffPrefix(uuid);
    }

    public Component getPrefixAll() {
        //return prefixAll;
        return Utility.getPrefix(uuid, false);
    }

    public String getReplyTarget() {
        return replyTarget;
    }

    public void setReplyTarget(String replyTarget) {
        this.replyTarget = replyTarget;
    }

    public List<Mail> getMails() {
        return mails;
    }

    public List<Mail> getUnReadMail() {
        return getMails().stream()
                .filter(Mail::isUnRead)
                .collect(Collectors.toList());
    }

    public void addMail(Mail mail) {
        mails.add(mail);
    }

    public List<UUID> getIgnoredPlayers() {
        return ignoredPlayers;
    }

    public void addIgnoredPlayers(UUID uuid) {
        ignoredPlayers.add(uuid);
    }

    public void removeIgnoredPlayers(UUID uuid) {
        ignoredPlayers.remove(uuid);
    }

    public List<UUID> getIgnoredBy() {
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

    public boolean isSpy() {
        return spy;
    }

    public void toggleSpy() {
        this.spy = !spy;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public void setMuted(boolean muted) {
        this.isMuted = muted;
    }

    public void reloadDisplayName() {
        name = Queries.getDisplayName(uuid);
        if (name == null) {
            name = Utility.getDisplayName(uuid, "");
        }
        setDisplayName(name);
    }
}
