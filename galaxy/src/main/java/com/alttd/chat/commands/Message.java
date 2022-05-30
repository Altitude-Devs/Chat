package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Message implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) {
            return true;
        }
        if(args.length < 2) return false; // todo error message or command info

        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        user.setReplyContinueTarget(args[0]);
        String message = StringUtils.join(args, " ", 1, args.length);
        ChatPlugin.getInstance().getChatHandler().privateMessage(player, args[0], message);
        return false;
    }

}
