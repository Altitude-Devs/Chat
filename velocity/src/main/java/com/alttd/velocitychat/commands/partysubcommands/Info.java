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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
            Optional<Player> optionalPlayer = VelocityChat.getPlugin().getProxy().getPlayer(partyUser.getUuid());
            if (optionalPlayer.isPresent() && optionalPlayer.get().isActive())
                displayNames.add(Config.ONLINE_PREFIX.append(partyUser.getDisplayName()));
            else
                displayNames.add(Config.OFFLINE_PREFIX.append(partyUser.getDisplayName()));
        }

        PartyUser owner = party.getPartyUser(party.getOwnerUuid());
        source.sendMessage(Utility.parseMiniMessage(Config.PARTY_INFO,
                Placeholder.unparsed("party", party.getPartyName()),
                Placeholder.unparsed("password", party.getPartyPassword()),
                Placeholder.component("owner", owner == null ? MiniMessage.miniMessage().deserialize("Unknown Owner") : owner.getDisplayName()),
                Placeholder.component("members", Component.join(JoinConfiguration.separator(Component.text(", ")), displayNames))
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
