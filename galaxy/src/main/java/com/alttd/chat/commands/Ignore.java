package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class Ignore implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(!(sender instanceof Player)) { // must be a player
            return true;
        }
        if(args.length > 1) return false; // todo error message or command info
        String targetName = args[0];
        Player targetPlayer = Bukkit.getPlayer(targetName);
        if(targetPlayer == null)  { // can't ignore offline players
            //sender.sendMessage("Target not found..."); // TODO load from config and minimessage
            return false;
        }
        if(targetPlayer.hasPermission("chat.ignorebypass")) {
            sender.sendMessage("You can't ignore this player"); // TODO load from config and minimessage
            return false;
        }
        UUID target = targetPlayer.getUniqueId();
        new BukkitRunnable() {
            @Override
            public void run() {
                ChatUser chatUser = ChatUserManager.getChatUser(((Player) sender).getUniqueId());
                if(!chatUser.getIgnoredPlayers().contains(target)) {
                    chatUser.addIgnoredPlayers(target);
                    Queries.ignoreUser(((Player) sender).getUniqueId(), target);
                    sender.sendMessage("You have turned ignored " + targetName + "."); // TODO load from config and minimessage
                } else {
                    sender.sendMessage("You have already ignored " + targetName + "."); // TODO load from config and minimessage
                }
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());
        return false;
    }

}
