package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.managers.ChatUserManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class GlobalChat implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) { // must be a player
            return true;
        }
        Player player = (Player) sender;
        if(args.length == 0) return false;
        if(args[0].equalsIgnoreCase("togglegc")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    Objects.requireNonNull(ChatUserManager.getChatUser(((Player) sender).getUniqueId())).toggleGc();

                    String message = StringUtils.join(args, " ", 0, args.length);
                    ChatPlugin.getInstance().getChatHandler().globalChat(player, message);
                }
            }.runTask(ChatPlugin.getInstance());
        }
        return false;
    }

}
