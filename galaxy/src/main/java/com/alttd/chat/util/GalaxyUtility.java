package com.alttd.chat.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class GalaxyUtility {
    public static void sendBlockedNotification(String prefix, Player player, String input, String target) {
        MiniMessage miniMessage = MiniMessage.get();
        Bukkit.getOnlinePlayers().forEach(a ->{
            Component blockedNotification = miniMessage.parse("<red>[" + prefix + "] "
                    + Utility.getDisplayName(player.getUniqueId(), player.getName())
                    + (target.isEmpty() ? " tried to say: " : " -> " + target + ": ")
                    + input + "</red>");
            if (a.hasPermission("chat.alert-blocked")) {
                a.sendMessage(blockedNotification);//TODO make configurable (along with all the messages)
            }
        });
        player.sendMessage(miniMessage.parse("<red>The language you used in your message is not allowed, " +
                "this constitutes as your only warning. Any further attempts at bypassing the filter will result in staff intervention.</red>"));
    }

    public static void sendBlockedNotification(String prefix, Player player, Component input, String target) {
        sendBlockedNotification(prefix, player, PlainComponentSerializer.plain().serialize(input), target);
    }
}
