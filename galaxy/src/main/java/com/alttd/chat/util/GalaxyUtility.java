package com.alttd.chat.util;

import com.alttd.chat.config.Config;
import com.alttd.chat.managers.RegexManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GalaxyUtility {

    public static void sendBlockedNotification(String prefix, Player player, String input, String target) {
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.parsed("prefix", prefix),
                Placeholder.parsed("displayname", Utility.getDisplayName(player.getUniqueId(), player.getName())),
                Placeholder.parsed("target", (target.isEmpty() ? " tried to say: " : " -> " + target + ": ")),
                Placeholder.parsed("input", input)
        );
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
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.parsed("prefix", prefix),
                Placeholder.parsed("displayname", Utility.getDisplayName(player.getUniqueId(), player.getName())),
                Placeholder.parsed("target", (target.isEmpty() ? " tried to say: " : " -> " + target + ": ")),
                Placeholder.component("input", input)
        );
        Component blockedNotification = Utility.parseMiniMessage(Config.NOTIFICATIONFORMAT, placeholders);

        Bukkit.getOnlinePlayers().forEach(a ->{
            if (a.hasPermission("chat.alert-blocked")) {
                a.sendMessage(blockedNotification);
            }
        });
        player.sendMessage(Utility.parseMiniMessage("<red>The language you used in your message is not allowed, " +
                "this constitutes as your only warning. Any further attempts at bypassing the filter will result in staff intervention.</red>"));
    }

    public static void addAdditionalChatCompletions(Player player) {
        List<String> completions = new ArrayList<>(RegexManager.emotesList);
        Utility.formattingPerms.forEach((perm, pair) -> {
            if (player.hasPermission(perm)) {
                completions.addAll(pair.getY());
            }
        });
        player.addAdditionalChatCompletions(completions);
    }

}
