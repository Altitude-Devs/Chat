package com.alttd.chat.commands;

import com.alttd.chat.objects.EmoteList;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Emotes implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if(!(sender instanceof Player player)) { // must be a player
            return true;
        }
        EmoteList emoteList = EmoteList.getEmoteList(player.getUniqueId());
        if(args.length > 0) {
            try {
                int page = Integer.parseInt(args[0]);
                emoteList.setPage(page);
            } catch (NumberFormatException ignored) {
                switch (args[0].toLowerCase()) {
                    case "next" -> emoteList.nextPage();
                    case "prev", "previous" -> emoteList.prevPage();
                    default -> {}
                }
            }
        }
        player.sendMessage(emoteList.showEmotePage());
        return false;
    }
}
