package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class GlobalChat implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String message = StringUtils.join(args, " ", 0, args.length);
        ChatPlugin.getInstance().getChatHandler().globalChat(sender, message);
        return false;
    }

}
