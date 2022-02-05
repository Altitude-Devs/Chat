package com.alttd.chat.util;

import com.alttd.chat.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.placeholder.Replacement;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class GalaxyUtility {
    public static void sendBlockedNotification(String prefix, Player player, String input, String target) {
        Map<String, Replacement<?>> placeholders = new HashMap<>();
        placeholders.put("prefix", Replacement.miniMessage(prefix));
        placeholders.put("displayname", Replacement.miniMessage(Utility.getDisplayName(player.getUniqueId(), player.getName())));
        placeholders.put("target", Replacement.miniMessage((target.isEmpty() ? " tried to say: " : " -> " + target + ": ")));
        placeholders.put("input", Replacement.miniMessage(input));
        Component blockedNotification = Utility.parseMiniMessage(Config.NOTIFICATIONFORMAT, placeholders);

        Bukkit.getOnlinePlayers().forEach(a ->{
            if (a.hasPermission("chat.alert-blocked")) {
                a.sendMessage(blockedNotification);
            }
        });
        player.sendMessage(Utility.parseMiniMessage("<red>The language you used in your message is not allowed, " +
                "this constitutes as your only warning. Any further attempts at bypassing the filter will result in staff intervention.</red>"));
    }

    public static void sendBlockedNotification(String prefix, Player player, Component input, String target) {
        sendBlockedNotification(prefix, player, PlainTextComponentSerializer.plainText().serialize(input), target);
    }
}
