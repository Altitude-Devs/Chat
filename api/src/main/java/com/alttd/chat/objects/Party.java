package com.alttd.chat.objects;

import com.alttd.chat.database.Queries;

import java.util.HashMap;
import java.util.UUID;

public class Party {

    private final int partyId;
    private UUID ownerUuid;
    private String partyName;
    private String partyPassword;
    private HashMap<UUID, ChatUser> partyUsers; //TODO might need to be a map?

    public Party(int partyId, UUID ownerUuid, String partyName, String partyPassword) {
        this.partyId = partyId;
        this.ownerUuid = ownerUuid;
        this.partyName = partyName;
        this.partyPassword = partyPassword;
        partyUsers = new HashMap<>();
    }

    public void addUser(HashMap<UUID, ChatUser> partyUsers) {
        this.partyUsers.putAll(partyUsers);
    }

    public void addUser(ChatUser partyUser) {
        this.partyUsers.put(partyUser.getUuid(), partyUser);
        Queries.addUser(partyUser);
    }

    public void removeUser(ChatUser partyUser) {
        partyUsers.remove(partyUser.getUuid());
        Queries.removeUser(partyUser.getUuid());
    }

    public int getPartyId() {
        return partyId;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public void setOwnerUuid(UUID ownerUuid) {
        this.ownerUuid = ownerUuid;
        Queries.setPartyOwner(ownerUuid, partyId); //TODO: Async pls
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
        Queries.setPartyName(partyName, partyId); //TODO: Async pls
    }

    public String getPartyPassword() {
        return partyPassword;
    }

    public void setPartyPassword(String partyPassword) {
        this.partyPassword = partyPassword;
        Queries.setPartyPassword(partyPassword, partyId); //TODO: Async pls
    }

    public boolean hasPartyPassword() {
        return !partyPassword.isEmpty();
    }

    public HashMap<UUID, ChatUser> getPartyUsers() {
        return partyUsers;
    }

    public void setPartyUsers(HashMap<UUID, ChatUser> partyUsers) {
        this.partyUsers = partyUsers;
    }
}
