package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.util.Utility;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Spy implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) { // must be a player
            return true;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                UUID uuid = ((Player) sender).getUniqueId();
                Utility.flipPermission(uuid, Config.SPYPERMISSION);
                //Queries.setGlobalChatState(chatUser.isGcOn(), chatUser.getUuid());
                sender.sendMessage(MiniMessage.get().parse("You have turned spy " + (!Utility.hasPermission(uuid, Config.GCPERMISSION) ? "<green>on." : "<red>off."))); // TODO load from config and minimessage
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());
        return false;
    }

}
