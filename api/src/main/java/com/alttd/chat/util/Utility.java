package com.alttd.chat.util;

import com.alttd.chat.ChatAPI;
import com.alttd.chat.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.awt.*;
import java.util.*;

public class Utility {

    public static String stringRegen = "\\{#[A-Fa-f0-9]{6}(<)?(>)?}";
    public static HashMap<String, String> colors;
    static { // this might be in minimessage already?
        colors = new HashMap<>();
        colors.put("&0", "<black>"); // and confirm these are correct
        colors.put("&1", "<dark_blue>"); // could also add some default hex colors here?
        colors.put("&2", "<dark_green>");
        colors.put("&3", "<dark_aqua>");
        colors.put("&4", "<dark_red>");
        colors.put("&5", "<dark_purple>");
        colors.put("&6", "<gold>");
        colors.put("&7", "<gray>");
        colors.put("&8", "<dark_gray>");
        colors.put("&9", "<blue>");
        colors.put("&a", "<green>");
        colors.put("&b", "<aqua>");
        colors.put("&c", "<red>");
        colors.put("&d", "<light_purple>");
        colors.put("&e", "<yellow>");
        colors.put("&f", "<white>");
    }

    public static String parseColors(String message) {
        // split string in sections and check those vs looping hashmap?:/
        // think this is better, but will check numbers on this
        for (String key : colors.keySet()) {
            if (message.contains(key)) {
                message = message.replace(key, colors.get(key));
            }
        }
        return message;
    }

    public static Component getPrefix(UUID uuid, boolean single) {
        StringBuilder prefix = new StringBuilder();
        LuckPerms luckPerms = ChatAPI.get().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        if(user == null) return Component.empty();
        if(!single) {
            Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());
            inheritedGroups.stream()
                    .sorted(Comparator.comparingInt(o -> o.getWeight().orElse(0)))
                    .forEach(group -> {
                        if (Config.PREFIXGROUPS.contains(group.getName())) {
                            prefix.append(group.getCachedData().getMetaData().getPrefix());
                        }
                    });
        }
        prefix.append(user.getCachedData().getMetaData().getPrefix());

        return applyColor(prefix.toString());
    }

    public static Component getStaffPrefix(UUID uuid) {
        StringBuilder prefix = new StringBuilder();
        LuckPerms luckPerms = ChatAPI.get().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        if(user == null) return Component.empty();
        if(user.getCachedData().getPermissionData().checkPermission("group." + Config.MINIMIUMSTAFFRANK).asBoolean()) {
            Group group = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
            if(group != null)
                prefix.append(group.getCachedData().getMetaData().getPrefix());
        }
        return applyColor(prefix.toString());
    }

    public static String getDisplayName(UUID uuid) {
        OfflinePlayer player = Bukkit.getPlayer(uuid);
        if (player != null && player.hasPlayedBefore()) {
            return player.getName();
        }
        LuckPerms luckPerms = ChatAPI.get().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        if(user == null) return "";
        return user.getUsername();
    }

    public static void flipPermission(UUID uuid, String permission) {
        ChatAPI.get().getLuckPerms().getUserManager().modifyUser(uuid, user -> {
            // Add the permission
            user.data().add(Node.builder(permission)
                    .value(!user.getCachedData().getPermissionData().checkPermission(permission).asBoolean()).build());
        });
    }

    public static boolean hasPermission(UUID uuid, String permission) {
        LuckPerms luckPerms = ChatAPI.get().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        if(user == null) return false;
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public static Component applyColor(String message) {
        String hexColor1 = "";
        String hexColor2 = "";
        StringBuilder stringBuilder = new StringBuilder();
        message = parseColors(message);
        boolean startsWithColor = false;
        boolean lastColorMatters = false;

        if (message.matches(".*" + stringRegen + ".*")) {
            String[] split = message.split(stringRegen);

            ArrayList<String> list = new ArrayList<>();
            int nextIndex = 0;
            if (message.indexOf("}") <= 11) {
                startsWithColor = true;
                list.add(message.substring(0, message.indexOf("}") + 1));
            }
            for (String s : split) {
                nextIndex += s.length();
                int tmp = message.indexOf("}", nextIndex);
                if (tmp < message.length() && tmp>=0) {
                    list.add(message.substring(nextIndex, tmp + 1));
                    nextIndex = tmp + 1;
                }
            }

            int i;
            boolean firstLoop = true;
            if (startsWithColor) {
                i = -1;
            } else {
                i = 0;
                stringBuilder.append(split[i]);
            }

            for (String s : list) {
                boolean lesser = s.contains("<");
                boolean bigger = s.contains(">");

                if (bigger && lesser) {
                    hexColor2 = s.substring(1, s.length() - 3);
                } else if (bigger || lesser) {
                    hexColor2 = s.substring(1, s.length() - 2);
                } else {
                    hexColor2 = s.substring(1, s.length() -1);
                }

                if (firstLoop) {
                    lastColorMatters = bigger;
                    hexColor1 = hexColor2;
                    firstLoop = false;
                    i++;
                    continue;
                }

                if (lesser && lastColorMatters) {
                    stringBuilder.append("<gradient:").append(hexColor1).append(":").append(hexColor2).append(">").append(split[i]).append("</gradient>");
                } else {
                    stringBuilder.append("<").append(hexColor1).append(">").append(split[i]);
                }

                hexColor1 = hexColor2;
                lastColorMatters = bigger;
                i++;
            }
            if (split.length > i){
                stringBuilder.append("<").append(hexColor1).append(">").append(split[i]);
            }
        }
        MiniMessage miniMessage = MiniMessage.get();
        return stringBuilder.length()==0 ? miniMessage.parse(message)
                : miniMessage.parse(stringBuilder.toString());
    }

}
