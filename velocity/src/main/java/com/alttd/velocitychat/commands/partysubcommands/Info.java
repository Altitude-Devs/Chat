package com.alttd.velocitychat.commands.partysubcommands;

import com.alttd.chat.config.Config;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.Party;
import com.alttd.chat.objects.PartyUser;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.commands.SubCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.placeholder.Placeholder;

import java.util.ArrayList;
import java.util.List;

public class Info implements SubCommand {
    @Override
    public String getName() {
        return "info";
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

        List<Component> displayNames = new ArrayList<>();
        for (PartyUser partyUser : party.getPartyUsers()) {
            displayNames.add(partyUser.getDisplayName());
        }

        source.sendMessage(Utility.parseMiniMessage(Config.PARTY_INFO,
                Placeholder.miniMessage("party", party.getPartyName()),
                Placeholder.miniMessage("password", party.getPartyPassword()),
                Placeholder.component("owner", party.getPartyUser(party.getOwnerUuid()).getDisplayName()),
                Placeholder.component("members", Component.join(Component.text(", "), displayNames))
        ));
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Config.PARTY_HELP_INFO;
    }
}
