package com.alttd.velocitychat.commands.partysubcommands;

import com.alttd.chat.config.Config;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.Party;
import com.alttd.chat.objects.PartyUser;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.VelocityChat;
import com.alttd.velocitychat.commands.SubCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.Template;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Owner implements SubCommand {
    @Override
    public String getName() {
        return "owner";
    }

    @Override
    public void execute(String[] args, CommandSource source) {
        if (!(source instanceof Player player)) {
            source.sendMessage(Utility.parseMiniMessage(Config.NO_CONSOLE));
            return;
        }
        if (args.length != 2) {
            source.sendMessage(Utility.parseMiniMessage(getHelpMessage()));
            return;
        }
        Party party = PartyManager.getParty(player.getUniqueId());
        if (party == null) {
            source.sendMessage(Utility.parseMiniMessage(Config.NOT_IN_A_PARTY));
            return;
        }
        if (!party.getOwnerUuid().equals(player.getUniqueId())) {
            source.sendMessage(Utility.parseMiniMessage(Config.NOT_YOUR_PARTY));
            return;
        }
        PartyUser partyUser = party.getPartyUser(args[1]);
        if (partyUser == null) {
            source.sendMessage(Utility.parseMiniMessage(Config.NOT_A_PARTY_MEMBER, List.of(
                    Template.template("player", args[1])
            )));
            return;
        }
        party.setNewOwner(partyUser.getUuid());
        VelocityChat.getPlugin().getChatHandler().sendPartyMessage(party,
                Utility.parseMiniMessage(Config.NEW_PARTY_OWNER, List.of(
                        Template.template("old_owner", player.getUsername()),
                        Template.template("new_owner", partyUser.getPlayerName())
                )));
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        ArrayList<String> suggest = new ArrayList<>();
        if (!(source instanceof Player player))
            return suggest;
        UUID uuid = player.getUniqueId();
        Party party = PartyManager.getParty(uuid);
        if (party == null)
            return suggest;
        if (args.length == 1 || args.length == 2)
            suggest.addAll(party.getPartyUsers().stream()
                    .filter(partyUser -> !partyUser.getUuid().equals(uuid))
                    .map(PartyUser::getPlayerName).collect(Collectors.toList()));
        return suggest;
    }

    @Override
    public String getHelpMessage() {
        return Config.PARTY_HELP_OWNER;
    }
}
