package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.objects.Channel;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class ChatChannel extends BukkitCommand {

    Channel channel;
    String command;

    public ChatChannel(Channel channel) {
        super(channel.getChannelName().toLowerCase());
        this.channel = channel;
        this.command = channel.getChannelName().toLowerCase();
        this.description = "Chat channel named " + channel.getChannelName() + ".";
        this.usageMessage = "/" + command + " <message>";
        this.setAliases(Collections.emptyList());
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String command, @NotNull String[] args) {
        if(!(sender instanceof Player player)) { // must be a player
            return true;
        }
        if(args.length == 0) return false;

        String message = StringUtils.join(args, " ", 0, args.length);

        new BukkitRunnable() {
            @Override
            public void run() {
                ChatPlugin.getInstance().getChatHandler().chatChannel(player, channel, message);
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());

        return false;
    }
}
