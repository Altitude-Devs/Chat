package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.util.Utility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class MuteServer implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) { // must be a player
            return true;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                UUID uuid = player.getUniqueId();
                if (!Utility.hasPermission(uuid, Config.SERVERMUTEPERMISSION)) {
                    sender.sendMessage(Utility.parseMiniMessage("<red>You don't have permission to use this command.</red>"));
                    return;
                }

                ChatPlugin.getInstance().toggleServerMuted();

                Component component;
                if (ChatPlugin.getInstance().serverMuted()) {
                    component = Utility.parseMiniMessage(Utility.getDisplayName(player.getUniqueId(), player.getName()) + " <red>muted</red><white> chat.");
                } else {
                    component = Utility.parseMiniMessage(Utility.getDisplayName(player.getUniqueId(), player.getName()) + " <green>un-muted</green><white> chat.");
                }

                Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(component));
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());
        return false;
    }

}
