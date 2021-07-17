package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;

public class ToggleGlobalChat implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) { // must be a player
            return true;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                ChatUser chatUser = ChatUserManager.getChatUser(((Player) sender).getUniqueId());
                chatUser.toggleGc();
                sender.sendMessage("You have turned globalchat " + (chatUser.isGcOn() ? "<green>on." : "<red>off.")); // TODO load from config and minimessage
            }
        }.runTask(ChatPlugin.getInstance());
        return false;
    }

}
