package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Continue implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) {
            return true;
        }
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        if (user.getReplyContinueTarget() == null) return false;
        if(args.length == 0) return false; // todo error message or command info

        String message = StringUtils.join(args, " ", 0, args.length);
        ChatPlugin.getInstance().getChatHandler().continuePrivateMessage(player, user.getReplyContinueTarget(), message);
        return false;
    }
}
