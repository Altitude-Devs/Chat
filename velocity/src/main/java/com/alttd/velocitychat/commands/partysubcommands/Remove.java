package com.alttd.velocitychat.commands.partysubcommands;

import com.alttd.chat.config.Config;
import com.alttd.chat.managers.ChatUserManager;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class Remove implements SubCommand {
    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public void execute(String[] args, CommandSource source) {
        if (!(source instanceof Player player)) {
            source.sendMessage(Utility.parseMiniMessage(Config.NO_CONSOLE));
            return;
        }
        if (args.length < 2) {
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
        Optional<Player> optionalPlayer = VelocityChat.getPlugin().getProxy().getPlayer(args[1]);
        PartyUser partyUser;
        Player onlinePlayer = null;
        if (optionalPlayer.isEmpty()) {
            partyUser = party.getPartyUser(args[1]);
            if (partyUser == null) {
                source.sendMessage(Utility.parseMiniMessage(Config.NOT_A_PARTY_MEMBER, List.of(
                        Template.template("player", args[1])
                )));
                return;
            }
        } else {
            onlinePlayer = optionalPlayer.get();
            partyUser = party.getPartyUser(onlinePlayer.getUniqueId());
            if (partyUser == null) {
                source.sendMessage(Utility.parseMiniMessage(Config.NOT_A_PARTY_MEMBER, List.of(
                        Template.template("player", onlinePlayer.getUsername())
                )));
                return;
            }
        }
        party.removeUser(ChatUserManager.getChatUser(partyUser.getUuid()));

        if (partyUser.getUuid().equals(party.getOwnerUuid())) {
            source.sendMessage(Utility.parseMiniMessage(Config.CANT_REMOVE_PARTY_OWNER));
            return;
        }

        if (onlinePlayer != null && onlinePlayer.isActive()) {
            onlinePlayer.sendMessage(Utility.parseMiniMessage(Config.REMOVED_FROM_PARTY, List.of(
                    Template.template("party", party.getPartyName())
            )));
        }

        source.sendMessage(Utility.parseMiniMessage(Config.REMOVED_USER_FROM_PARTY, List.of(
                Template.template("player", onlinePlayer == null ? partyUser.getPlayerName() : onlinePlayer.getUsername())
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
        return Config.PARTY_HELP_REMOVE;
    }
}
