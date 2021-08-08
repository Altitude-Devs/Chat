package com.alttd.chat.objects;

import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.PartyManager;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;

import java.util.*;
import java.util.List;

public class Party {

    private final int partyId;
    private UUID ownerUuid;
    private String partyName;
    private String partyPassword;
    private final HashMap<UUID, String> partyUsers; //TODO might need to be a map?

    public Party(int partyId, UUID ownerUuid, String partyName, String partyPassword) {
        this.partyId = partyId;
        this.ownerUuid = ownerUuid;
        this.partyName = partyName;
        this.partyPassword = partyPassword;
        partyUsers = new HashMap<>();
    }

    public void putUser(UUID uuid, String displayName) {
        this.partyUsers.put(uuid, displayName);
    }

    public void addUser(ChatUser partyUser) {
        this.partyUsers.put(partyUser.getUuid(), PlainComponentSerializer.plain().serialize(partyUser.getDisplayName()));
        partyUser.setPartyId(getPartyId());
        Queries.addPartyUser(partyUser);
    }

    public void removeUser(UUID uuid) {
        removeUser(ChatUserManager.getChatUser(uuid));
    }

    public void removeUser(ChatUser partyUser) {
        partyUsers.remove(partyUser.getUuid());
        Queries.removePartyUser(partyUser.getUuid());
    }

    public int getPartyId() {
        return partyId;
    }

    public UUID getOwnerUuid() {
        return ownerUuid;
    }

    public UUID newOwner() {
        UUID uuid = partyUsers.keySet().iterator().next();
        setOwnerUuid(uuid);
        return uuid;
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

    public HashMap<UUID, String> getPartyUsers() {
        return partyUsers;
    }

    public void delete() {
        Queries.removeParty(partyId);
        PartyManager.removeParty(this);
    }

    public List<UUID> getPartyUsersUuid() {
        return new ArrayList<>(partyUsers.keySet());
    }

    public String getUserDisplayName(UUID uuid) {
        return partyUsers.get(uuid);
    }
}
