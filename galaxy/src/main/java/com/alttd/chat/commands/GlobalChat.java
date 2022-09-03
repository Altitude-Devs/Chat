package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GlobalChat implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) { // must be a player
            return true;
        }
        if(args.length == 0) return false;

        String message = StringUtils.join(args, " ", 0, args.length);

        new BukkitRunnable() {
            @Override
            public void run() {
                ChatPlugin.getInstance().getChatHandler().globalChat(player, message);
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());

        return false;
    }

}
