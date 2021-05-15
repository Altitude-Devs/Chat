package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GlobalChat implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) { // must be a player
            return true;
        }
        Player player = (Player) sender;
        String message = StringUtils.join(args, " ", 0, args.length);
        ChatPlugin.getInstance().getChatHandler().globalChat(player, message);
        return false;
    }

}
