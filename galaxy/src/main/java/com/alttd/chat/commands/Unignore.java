package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.database.Queries;
import com.alttd.chat.listeners.PluginMessage;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Unignore implements CommandExecutor {

    private final ChatPlugin plugin;

    public Unignore() {
        plugin = ChatPlugin.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) { // must be a player
            return true;
        }
        if(args.length > 1) return false; // todo error message or command info
        String targetName = args[0];
        UUID target = Bukkit.getOfflinePlayer(targetName).getUniqueId();
        if(target == null) {
            //sender.sendMessage("Target not found..."); // TODO load from config and minimessage
            return false;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                ChatUser chatUser = ChatUserManager.getChatUser(player.getUniqueId());
                if(chatUser.getIgnoredPlayers().contains(target)) {
                    chatUser.removeIgnoredPlayers(target);
                    Queries.unIgnoreUser(player.getUniqueId(), target);
                    sender.sendMessage("You no longer ignore " + targetName + "."); // TODO load from config and minimessage
                    sendPluginMessage("unignore", player, target);
                } else {
                    sender.sendMessage("You don't have " + targetName + " ignored."); // TODO load from config and minimessage
                }
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());
        return false;
    }

    private void sendPluginMessage(String channel, Player player, UUID target) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(channel);
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(target.toString());
        player.sendPluginMessage(plugin, Config.MESSAGECHANNEL, out.toByteArray());
    }
}
