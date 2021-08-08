package com.alttd.chat.managers;

import com.alttd.chat.database.Queries;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Party;
import net.kyori.adventure.text.Component;

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
        if (id < 0) return null;
        for(Party party : parties) {
            if(id == party.getPartyId()) {
                return party;
            }
        }
        return null;
    }

    public static Party getParty(String partyName) {
        for(Party party : parties) {
            if(party.getPartyName().equalsIgnoreCase(partyName)) {
                return party;
            }
        }
        return null;
    }

    public static Party getParty(UUID uuid) {
        for(Party party : parties) {
            if(party.getPartyUsersUuid().contains(uuid)) {
                return party;
            }
        }
        return null;
    }

    public static void loadParties() {
        Queries.loadParties();
    }
}
