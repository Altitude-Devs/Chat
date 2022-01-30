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
import net.kyori.adventure.text.minimessage.Template;

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

        List<Template> templates = new ArrayList<>(List.of(
                Template.template("party", party.getPartyName()),
                Template.template("password", party.getPartyPassword()),
                Template.template("owner", party.getPartyUser(party.getOwnerUuid()).getDisplayName()),
                Template.template("members", Component.join(Component.text(", "), displayNames))
        ));

        source.sendMessage(Utility.parseMiniMessage(Config.PARTY_INFO, templates));
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
