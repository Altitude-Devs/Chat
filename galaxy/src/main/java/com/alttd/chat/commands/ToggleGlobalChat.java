package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.util.Utility;
import jdk.jshell.execution.Util;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Objects;
import java.util.UUID;

public class ToggleGlobalChat implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) { // must be a player
            return true;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                UUID uuid = ((Player) sender).getUniqueId();
                ChatUser chatUser = ChatUserManager.getChatUser(uuid);
                //chatUser.toggleGc();
                Utility.flipPermission(uuid, Config.GCPERMISSION);
                //Queries.setGlobalChatState(chatUser.isGcOn(), chatUser.getUuid());
                sender.sendMessage(Utility.parseMiniMessage("You have turned globalchat " + (!Utility.hasPermission(uuid, Config.GCPERMISSION) ? "<green>on." : "<red>off."))); // TODO load from config and minimessage
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());
        return false;
    }

}
