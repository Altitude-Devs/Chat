package com.alttd.chat.commands;

import com.alttd.chat.config.Config;
import com.alttd.chat.objects.channels.CustomChannel;
import com.alttd.chat.util.ToggleableForCustomChannel;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ChatChannel extends BukkitCommand {

    CustomChannel channel;
    String command;
    ToggleableForCustomChannel toggleableForCustomChannel;
    private static List<ChatChannel> activeCommands = new ArrayList<>();

    public ChatChannel(CustomChannel channel) {
        super(channel.getChannelName().toLowerCase());
        this.channel = channel;
        this.command = channel.getChannelName().toLowerCase();
        this.description = "Chat channel named " + channel.getChannelName() + ".";
        this.usageMessage = "/" + command + " <message>";
        this.setAliases(Collections.emptyList());
        activeCommands.add(this);
        this.toggleableForCustomChannel = new ToggleableForCustomChannel(channel);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String command, @NotNull String[] args) {
        if(!(sender instanceof Player player)) { // must be a player
            return true;
        }

        if(args.length == 0) {
            player.sendMiniMessage(Config.PARTY_TOGGLED, TagResolver.resolver(
                    Placeholder.unparsed("channel", channel.getChannelName()),
                    Placeholder.unparsed("status", toggleableForCustomChannel.toggle(player.getUniqueId())
                            ? "<green>on</green>" : "<red>off</red>")));
            return false;
        }

        String message = StringUtils.join(args, " ", 0, args.length);

        toggleableForCustomChannel.sendMessage(player, message);

        return false;
    }
}
