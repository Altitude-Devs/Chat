package com.alttd.chat.util;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.objects.Toggleable;
import com.alttd.chat.objects.channels.CustomChannel;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.UUID;

public class ToggleableForCustomChannel extends Toggleable {

    private final CustomChannel customChannel;

    public ToggleableForCustomChannel(CustomChannel customChannel) {
        super();
        this.customChannel = customChannel;
    }

    private final HashSet<UUID> toggledUsers = new HashSet<>();

    @Override
    public boolean isToggled(UUID uuid) {
        return toggledUsers.contains(uuid);
    }

    @Override
    public void setOff(UUID uuid) {
        toggledUsers.remove(uuid);
    }

    @Override
    public void setOn(UUID uuid) {
        disableToggles(uuid);
        toggledUsers.add(uuid);
    }

    @Override
    public void sendMessage(Player player, String message) {
        new BukkitRunnable() {
            @Override
            public void run() {
                ALogger.info(String.format("%s sent %s message: %s", player.getName(), customChannel.getChannelName(), message));
                ChatPlugin.getInstance().getChatHandler().chatChannel(player, customChannel, message);
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());
    }
}
