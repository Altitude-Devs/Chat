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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Invite implements SubCommand {

    @Override
    public String getName() {
        return "invite";
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
        Optional<Player> optional = VelocityChat.getPlugin().getProxy().getPlayer(args[1]);
        if (optional.isEmpty()) {
            source.sendMessage(Utility.parseMiniMessage(Config.INVALID_PLAYER));
            return;
        }
        Player target = optional.get();

        if (!target.isActive()) {
            source.sendMessage(Utility.parseMiniMessage(Config.NOT_ONLINE, List.of(
                    Template.template("player", target.getUsername())
            )));
            return;
        }

        target.sendMessage(Utility.parseMiniMessage(Config.JOIN_PARTY_CLICK_MESSAGE, List.of(
                Template.template("party", party.getPartyName()),
                Template.template("party_password", party.getPartyPassword())
        )));
        source.sendMessage(Utility.parseMiniMessage(Config.SENT_PARTY_INV, List.of(
                Template.template("player", target.getUsername())
        )));
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        ArrayList<String> suggest = new ArrayList<>();
        if (!(source instanceof Player))
            return suggest;
        if (args.length == 1 || args.length == 2)
            suggest.addAll(VelocityChat.getPlugin().getProxy().getAllPlayers().stream()
                    .map(Player::getUsername)
                    .collect(Collectors.toList()));
        return suggest;
    }

    @Override
    public String getHelpMessage() {
        return Config.PARTY_HELP_INVITE;
    }
}
