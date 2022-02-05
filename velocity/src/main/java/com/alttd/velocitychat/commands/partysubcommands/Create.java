package com.alttd.velocitychat.commands.partysubcommands;

import com.alttd.chat.config.Config;
import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.Party;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.commands.SubCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.minimessage.placeholder.Placeholder;

import java.util.ArrayList;
import java.util.List;

public class Create implements SubCommand {

    @Override
    public String getName() {
        return "create";
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
        if (PartyManager.getParty(player.getUniqueId()) != null)
        {
            source.sendMessage(Utility.parseMiniMessage(Config.ALREADY_IN_PARTY));
            return;
        }
        if (PartyManager.getParty(args[1]) != null) {
            source.sendMessage(Utility.parseMiniMessage(Config.PARTY_EXISTS,
                    Placeholder.miniMessage("party", args[1])
            ));
            return;
        }
        Party party = Queries.addParty(player.getUniqueId(), args[1], args[2]);
//                        party.addUser(ChatUserManager.getChatUser(player.getUniqueId())); //Removed until we can get nicknames to translate to colors correctly
        party.addUser(ChatUserManager.getChatUser(player.getUniqueId()), player.getUsername());
        PartyManager.addParty(party);
        source.sendMessage(Utility.parseMiniMessage(Config.CREATED_PARTY,
                Placeholder.miniMessage("party_name", party.getPartyName()),
                Placeholder.miniMessage("party_password", party.getPartyPassword())));
    }

    @Override
    public List<String> suggest(String[] args, CommandSource source) {
        return new ArrayList<>();
    }

    @Override
    public String getHelpMessage() {
        return Config.PARTY_HELP_CREATE;
    }
}
