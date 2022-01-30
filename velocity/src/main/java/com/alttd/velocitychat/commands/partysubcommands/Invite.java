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

import java.util.List;
import java.util.Optional;

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

        target.sendMessage(Utility.parseMiniMessage("<click:run_command:'/chatparty join " + party.getPartyName() + " " + party.getPartyPassword() +
                "'><dark_aqua>You received an invite to join " + party.getPartyName() + " click this message to accept.</dark_aqua></click>"));
        source.sendMessage(Utility.parseMiniMessage("<green>You send a chat party invite to <player>!</green>", List.of(
                Template.template("player", target.getUsername())
        )));
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
