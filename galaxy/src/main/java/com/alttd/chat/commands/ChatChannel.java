package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.objects.channels.CustomChannel;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ChatChannel extends BukkitCommand {

    CustomChannel channel;
    String command;
    private static List<ChatChannel> activeCommands = new ArrayList<>();
    private HashSet<UUID> toggledUsers = new HashSet<>();

    public ChatChannel(CustomChannel channel) {
        super(channel.getChannelName().toLowerCase());
        this.channel = channel;
        this.command = channel.getChannelName().toLowerCase();
        this.description = "Chat channel named " + channel.getChannelName() + ".";
        this.usageMessage = "/" + command + " <message>";
        this.setAliases(Collections.emptyList());
        activeCommands.add(this);
    }

    public static ChatChannel getActiveChannel(UUID uuid) {
        for (ChatChannel activeCommand : activeCommands) {
            if (activeCommand.toggledUsers.contains(uuid))
                return activeCommand;
        }
        return (null);
    }

    private void toggleChannel(UUID uuid) {
        if (toggledUsers.contains(uuid)) {
            toggledUsers.remove(uuid);
            return;
        }
        ChatChannel activeChannel = getActiveChannel(uuid);
        if (activeChannel == null) {
            toggledUsers.add(uuid);
            return;
        }
        activeChannel.toggleChannel(uuid);
        toggledUsers.add(uuid);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String command, @NotNull String[] args) {
        if(!(sender instanceof Player player)) { // must be a player
            return true;
        }
        if(args.length == 0) {
            toggleChannel(player.getUniqueId());
            return false;
        }

        String message = StringUtils.join(args, " ", 0, args.length);

        sendChannelMessage(message, player);

        return false;
    }

    public void sendChannelMessage(Component message, Player player) {
        sendChannelMessage(PlainTextComponentSerializer.plainText().serialize(message), player);
    }

    public void sendChannelMessage(String message, Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ChatPlugin.getInstance().getChatHandler().chatChannel(player, channel, message);
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());
    }
}
