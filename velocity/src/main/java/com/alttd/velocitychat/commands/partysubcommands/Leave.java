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
import net.kyori.adventure.text.minimessage.Template;

import java.util.ArrayList;
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
                UUID uuid = party.setNewOwner();
                source.sendMessage(Utility.parseMiniMessage(Config.NOTIFY_FINDING_NEW_OWNER));
                VelocityChat.getPlugin().getChatHandler().sendPartyMessage(party,
                        Utility.parseMiniMessage(Config.OWNER_LEFT_PARTY, List.of(
                                Template.template("old_owner", player.getUsername()),
                                Template.template("new_owner", party.getPartyUser(uuid).getPlayerName())
                        )));
            } else {
                party.delete();
            }
        } else {
            source.sendMessage(Utility.parseMiniMessage(Config.LEFT_PARTY));
        }
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Config.PARTY_HELP_LEAVE;
    }
}
