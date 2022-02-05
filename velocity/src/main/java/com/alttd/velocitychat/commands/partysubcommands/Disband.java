package com.alttd.velocitychat.commands.partysubcommands;

import com.alttd.chat.config.Config;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.Party;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.VelocityChat;
import com.alttd.velocitychat.commands.SubCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.placeholder.Placeholder;

import java.util.ArrayList;
import java.util.List;

public class Disband implements SubCommand {
    @Override
    public String getName() {
        return "disband";
    }

    @Override
    public void execute(String[] args, CommandSource source) {
        if (!(source instanceof Player player)) {
            source.sendMessage(Utility.parseMiniMessage(Config.NO_CONSOLE));
            return;
        }
        if (args.length != 1 && args.length != 3) {
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
        if (args.length == 1) {
            source.sendMessage(Utility.parseMiniMessage(Config.DISBAND_PARTY_CONFIRM, Placeholder.miniMessage("party", party.getPartyName())));
            return;
        }
        if (!args[1].equalsIgnoreCase("confirm") || !args[2].equals(party.getPartyName())) {
            source.sendMessage(Utility.parseMiniMessage(getHelpMessage()));
            return;
        }
        VelocityChat.getPlugin().getChatHandler().sendPartyMessage(party,
                Utility.parseMiniMessage(Config.DISBANDED_PARTY,
                        Placeholder.miniMessage("owner", player.getUsername()),
                        Placeholder.miniMessage("party", party.getPartyName())
                ), null);
        party.delete();
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Config.PARTY_HELP_DISBAND;
    }
}
