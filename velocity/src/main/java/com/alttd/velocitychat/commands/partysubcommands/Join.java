package com.alttd.velocitychat.commands.partysubcommands;

import com.alttd.chat.config.Config;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Party;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.VelocityChat;
import com.alttd.velocitychat.commands.SubCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.minimessage.placeholder.Placeholder;

import java.util.ArrayList;
import java.util.List;

public class Join implements SubCommand {

    @Override
    public String getName() {
        return "join";
    }

    @Override
    public void execute(String[] args, CommandSource source) {
        if (!(source instanceof Player player)) {
            source.sendMessage(Utility.parseMiniMessage(Config.NO_CONSOLE));
            return;
        }
        if (args.length < 3 || !args[1].matches("[\\w]{3,16}") || !args[2].matches("[\\w]{3,16}")) {
            source.sendMessage(Utility.parseMiniMessage(getHelpMessage()));
            return;
        }

        Party party = PartyManager.getParty(args[1]);
        if (party == null) {
            source.sendMessage(Utility.parseMiniMessage(Config.NOT_A_PARTY));
            return;
        }
        if (!party.getPartyPassword().equals(args[2])) {
            source.sendMessage(Utility.parseMiniMessage(Config.INVALID_PASSWORD));
            return;
        }

//      party.addUser(ChatUserManager.getChatUser(player.getUniqueId())); //Removed until we can get nicknames to translate to colors correctly
        ChatUser chatUser = ChatUserManager.getChatUser(player.getUniqueId());
        if (chatUser.getPartyId() == party.getPartyId()) {
            source.sendMessage(Utility.parseMiniMessage(Config.ALREADY_IN_THIS_PARTY, Placeholder.miniMessage("party", party.getPartyName())));
            return;
        }
        party.addUser(chatUser, player.getUsername());
        source.sendMessage(Utility.parseMiniMessage(Config.JOINED_PARTY, Placeholder.miniMessage("party_name", party.getPartyName())));
        VelocityChat.getPlugin().getChatHandler().sendPartyMessage(party,
                Utility.parseMiniMessage(Config.PLAYER_JOINED_PARTY,
                        Placeholder.component("player_name", chatUser.getDisplayName()),
                        Placeholder.miniMessage("party_name", party.getPartyName())
                ), null);
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Config.PARTY_HELP_JOIN;
    }
}
