package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.objects.Toggleable;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.UUID;

public class PartyChat extends Toggleable implements CommandExecutor {

    private final HashSet<UUID> toggledUsers = new HashSet<>();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) { // must be a player
            return true;
        }

        if(args.length == 0) {
            player.sendMiniMessage(Config.PARTY_TOGGLED, Placeholder.component("status",
                    toggle(player.getUniqueId()) ? Config.TOGGLED_ON : Config.TOGGLED_OFF));
            return true;
        }

        String message = StringUtils.join(args, " ", 0, args.length);

        new BukkitRunnable() {
            @Override
            public void run() {
                ChatPlugin.getInstance().getChatHandler().partyMessage(player, message);
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());

        return true;
    }

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
                ChatPlugin.getInstance().getChatHandler().partyMessage(player, message);
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());
    }
}
