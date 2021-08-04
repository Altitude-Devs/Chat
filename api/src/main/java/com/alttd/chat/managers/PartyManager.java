package com.alttd.chat.managers;

import com.alttd.chat.database.Queries;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Party;

import java.util.ArrayList;
import java.util.UUID;

public class PartyManager {

    private static ArrayList<Party> parties;

    public static void initialize() {
        parties = new ArrayList<>();
        loadParties();
    }

    public static void addParty(Party party) {
        parties.add(party);
    }

    public static void removeParty(Party party) {
        parties.remove(party);
    }

    public static Party getParty(int id) {
        for(Party party : parties) {
            if(id == party.getPartyId()) {
                return party;
            }
        }
        return null;
    }

    public static Party getParty(UUID uuid) {
        return getParty(ChatUserManager.getChatUser(uuid).getPartyId());
    }

    public static void loadParties() {
        Queries.loadParties();
        for (ChatUser chatUser : ChatUserManager.getChatUsers()) {
            Party party = getParty(chatUser.getPartyId());
            if (party != null) party.addUser(chatUser);
        }
    }
}
