package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class Ignore implements CommandExecutor {

    private final ChatPlugin plugin;

    public Ignore() {
        plugin = ChatPlugin.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player player)) { // must be a player
            return true;
        }
        if(args.length > 1) return false; // todo error message or command info
        String targetName = args[0];
        if (targetName.equals("?")) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    ChatUser chatUser = ChatUserManager.getChatUser(player.getUniqueId());
                    HashSet<String> userNames = Queries.getUserNames(chatUser.getIgnoredPlayers());
                    StringBuilder ignoredMessage = new StringBuilder();

                    if (userNames.isEmpty()) {
                        player.sendMessage(MiniMessage.get().parse("You don't have anyone ignored!")); //TODO load from config
                        return;
                    }

                    ignoredMessage.append("You have the following users ignored:\n");
                    userNames.forEach(username -> ignoredMessage.append(username).append("\n"));
                    ignoredMessage.delete(ignoredMessage.length() - 1, ignoredMessage.length());

                    player.sendMessage(MiniMessage.get().parse(ignoredMessage.toString()));
                }
            }.runTaskAsynchronously(plugin);
            return false;
        }

        Player targetPlayer = Bukkit.getPlayer(targetName);
        if(targetPlayer == null)  { // can't ignore offline players
            sender.sendMessage("You can't ignore offline players");
            //sender.sendMessage("Target not found..."); // TODO load from config and minimessage
            return false;
        }

        UUID target = targetPlayer.getUniqueId();
        if(targetPlayer.hasPermission("chat.ignorebypass") || target.equals(player.getUniqueId())) {
            sender.sendMessage("You can't ignore this player"); // TODO load from config and minimessage
            return false;
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                ChatUser chatUser = ChatUserManager.getChatUser(player.getUniqueId());
                if(!chatUser.getIgnoredPlayers().contains(target)) {
                    chatUser.addIgnoredPlayers(target);
                    Queries.ignoreUser(((Player) sender).getUniqueId(), target);
                    sender.sendMessage("You have ignored " + targetName + "."); // TODO load from config and minimessage
                    sendPluginMessage("ignore", player, target);
                } else {
                    sender.sendMessage("You already have " + targetName + " ignored, to unignore use /unignore " + targetName + "."); // TODO load from config and minimessage
                }
            }
        }.runTaskAsynchronously(plugin);
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
