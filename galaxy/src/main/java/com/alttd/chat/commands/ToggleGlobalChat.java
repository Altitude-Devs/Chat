package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import net.kyori.adventure.text.minimessage.MiniMessage;
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
                Queries.setGlobalChatState(chatUser.isGcOn(), chatUser.getUuid());
                sender.sendMessage(MiniMessage.get().parse("You have turned globalchat " + (chatUser.isGcOn() ? "<green>on." : "<red>off."))); // TODO load from config and minimessage
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());
        return false;
    }

}
