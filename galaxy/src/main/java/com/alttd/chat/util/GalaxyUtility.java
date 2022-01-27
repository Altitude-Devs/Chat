package com.alttd.chat.util;

import com.alttd.chat.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GalaxyUtility {
    public static void sendBlockedNotification(String prefix, Player player, String input, String target) {
        List<Template> templates = new ArrayList<>(List.of(
                Template.template("prefix", prefix),
                Template.template("displayname", Utility.getDisplayName(player.getUniqueId(), player.getName())),
                Template.template("target", (target.isEmpty() ? " tried to say: " : " -> " + target + ": ")),
                Template.template("input", input)
        ));
        Component blockedNotification = Utility.parseMiniMessage(Config.NOTIFICATIONFORMAT, templates);

        Bukkit.getOnlinePlayers().forEach(a ->{
            if (a.hasPermission("chat.alert-blocked")) {
                a.sendMessage(blockedNotification);
            }
        });
        player.sendMessage(Utility.parseMiniMessage("<red>The language you used in your message is not allowed, " +
                "this constitutes as your only warning. Any further attempts at bypassing the filter will result in staff intervention.</red>"));
    }

    public static void sendBlockedNotification(String prefix, Player player, Component input, String target) {
        sendBlockedNotification(prefix, player, PlainComponentSerializer.plain().serialize(input), target);
    }
}
