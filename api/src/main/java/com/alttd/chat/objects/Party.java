package com.alttd.chat.objects;

import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.PartyManager;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Party {

    private final int partyId;
    private UUID ownerUuid;
    private String partyName;
    private String partyPassword;
    private static ArrayList<PartyUser> partyUsers;

    public Party(int partyId, UUID ownerUuid, String partyName, String partyPassword) {
        this.partyId = partyId;
        this.ownerUuid = ownerUuid;
        this.partyName = partyName;
        this.partyPassword = partyPassword;
        partyUsers = new ArrayList<>();
    }

    public void putPartyUser(PartyUser partyUser) {
        partyUsers.add(partyUser);
    }

    public void addUser(ChatUser chatUser, String playerName) {
        partyUsers.add(new PartyUser(chatUser.getUuid(), chatUser.getDisplayName(), playerName));
        chatUser.setPartyId(getPartyId());
        Queries.addPartyUser(chatUser);
    }

    public void removeUser(UUID uuid) {
        removeUser(ChatUserManager.getChatUser(uuid));
    }

    public void removeUser(ChatUser chatUser) {
        UUID uuid = chatUser.getUuid();
        Optional<PartyUser> first = partyUsers.stream()
                .filter(partyUser -> partyUser.getUuid().equals(uuid))
                .findFirst();
        if (first.isEmpty()) return;
        partyUsers.remove(first.get());
        Queries.removePartyUser(uuid);
    }

    public int getPartyId() {
        return partyId;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setNewOwner(UUID uuid) {
        setOwnerUuid(uuid);
    }

    public UUID setNewOwner() {
        UUID uuid = partyUsers.iterator().next().getUuid();
        setOwnerUuid(uuid);
        return uuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        Queries.setPartyOwner(ownerUuid, partyId);
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
        Queries.setPartyName(partyName, partyId);
    }

    public String getPartyPassword() {
        return partyPassword;
    }

    public void setPartyPassword(String partyPassword) {
        this.partyPassword = partyPassword;
        Queries.setPartyPassword(partyPassword, partyId);
    }

    public boolean hasPartyPassword() {
        return !partyPassword.isEmpty();
    }

    public List<PartyUser> getPartyUsers() {
        return partyUsers;
    }

    public void delete() {
        Queries.removeParty(partyId);
        PartyManager.removeParty(this);
    }

    public List<UUID> getPartyUsersUuid() {
        return partyUsers.stream().map(PartyUser::getUuid).collect(Collectors.toList());
    }

    public void resetPartyUsers() { // FIXME: 08/08/2021 This is a temp solution until bungee messages take over updating parties
        partyUsers.clear();
    }

    public PartyUser getPartyUser(UUID uuid) {
        for(PartyUser user : partyUsers) {
            if(uuid.equals(user.getUuid())) {
                return user;
            }
        }
        return null;
    }

    public PartyUser getPartyUser(String name) {
        for(PartyUser user : partyUsers) {
            if(name.equalsIgnoreCase(user.getPlayerName())) {
                return user;
            }
        }
        return null;
    }
}
