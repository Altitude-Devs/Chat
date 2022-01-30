package com.alttd.velocitychat.commands.partysubcommands;

import com.alttd.chat.config.Config;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.Party;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.VelocityChat;
import com.alttd.velocitychat.commands.SubCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.Template;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class Name implements SubCommand {
    @Override
    public String getName() {
        return "name";
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
        if (!args[1].matches("[\\w]{3,16}")) {
            source.sendMessage(Utility.parseMiniMessage(getHelpMessage()));
            return;
        }
        if (PartyManager.getParty(args[1]) != null) {
            source.sendMessage(Utility.parseMiniMessage(Config.PARTY_EXISTS, List.of(
                    Template.template("party", args[1])
            )));
            return;
        }
        party.setPartyName(args[1]);
        VelocityChat.getPlugin().getChatHandler().sendPartyMessage(party, Utility.parseMiniMessage(Config.RENAMED_PARTY), null);
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Config.PARTY_HELP_NAME;
    }
}
