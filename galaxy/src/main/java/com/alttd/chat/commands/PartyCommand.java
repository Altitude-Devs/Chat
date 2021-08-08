package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.Party;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class PartyCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) { // must be a player
            return true;
        }
        if(args.length == 0) return false;

        new BukkitRunnable() {
            @Override
            public void run() {
                switch (args[0].toLowerCase()) {
                    case "create" -> {
                        // TODO: 06/08/2021 verify args 1, 2 and check args length (3-16 char limit?)
                        Party party = Queries.addParty(player.getUniqueId(), args[1], args[2]);
                        PartyManager.addParty(party);
                    }
                    case "invite" -> {
                        // TODO: 07/08/2021 send invite to user, when they click execute </party join partyname password>
                    }
                    case "join" -> {
                        // TODO: 07/08/2021 verify password and join party
                    }
                    case "leave" -> {
                        // TODO: 07/08/2021 leave the party
                    }
                    case "remove" -> {
                        // TODO: 07/08/2021 remove specified user
                    }
                    case "delete" -> {
                        // TODO: 07/08/2021 ask for confirmation, when they click repeat the command but with --confirm (so if it has that at the end obv delete the party)
                    }
                    default -> {
                        // TODO: 07/08/2021 send help message
                    }
                }
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());

        return false;
    }
}
