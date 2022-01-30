package com.alttd.velocitychat.commands.partysubcommands;

import com.alttd.chat.config.Config;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.Party;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.VelocityChat;
import com.alttd.velocitychat.commands.SubCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Leave implements SubCommand {

    @Override
    public String getName() {
        return "leave";
    }

    @Override
    public void execute(String[] args, CommandSource source) {
        if (!(source instanceof Player player)) {
            source.sendMessage(Utility.parseMiniMessage(Config.NO_CONSOLE));
            return;
        }
        Party party = PartyManager.getParty(player.getUniqueId());
        if (party == null) {
            source.sendMessage(Utility.parseMiniMessage(Config.NOT_IN_A_PARTY));
            return;
        }
        Optional<ServerConnection> currentServer = player.getCurrentServer();
        if (currentServer.isEmpty())
            return;

        party.removeUser(player.getUniqueId());
        if (party.getOwnerUuid().equals(player.getUniqueId())) {
            if (party.getPartyUsers().size() > 0) {
                UUID uuid = party.newOwner();
                source.sendMessage(Utility.parseMiniMessage(Config.NOTIFY_FINDING_NEW_OWNER));
                VelocityChat.getPlugin().getChatHandler().partyMessage(party, player, "<dark_aqua>" +
                        player.getUsername() +
                        " left the chat party, the new party owner is " + party.getPartyUser(uuid).getPlayerName(),
                        currentServer.get());
            } else {
                party.delete();
            }
        } else {
            source.sendMessage(Utility.parseMiniMessage(Config.LEFT_PARTY));
        }
//        update(player, party.getPartyId()); TODO update party
    }

    @Override
    public List<String> suggest(String[] args) {
        return null;
    }

    @Override
    public String getHelpMessage() {
        return null;
    }
}
