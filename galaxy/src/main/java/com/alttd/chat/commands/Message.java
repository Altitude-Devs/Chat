package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Message  implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) { // must be a player, @teri should console be able to /msg?
            return true;
        }
        Player player = (Player) sender;
        if(args.length > 2) return false; // todo error message or command info

        String message = StringUtils.join(args, " ", 1, args.length);
        ChatPlugin.getInstance().getChatHandler().privateMessage(player, args[0], message);
        return false;
    }

}
