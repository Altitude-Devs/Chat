package com.alttd.chat.util;

import com.alttd.chat.VelocityChat;
import com.alttd.chat.config.Config;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.UUID;

public class Utility {

    public static HashMap<String, String> colors;
    static { // this might be in minimessage already?
        colors = new HashMap<>(); // todo map all colors to minimessage
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
        colors.put("&g", "<minecoin_gold>"); // is this a thing?
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

    public static String getPrefix(UUID uuid, boolean highest) {
        StringBuilder prefix = new StringBuilder();
        LuckPerms luckPerms = VelocityChat.getPlugin().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        if(user == null) return "";
        if(!highest) {
            Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());
            inheritedGroups.stream()
                    .sorted(Comparator.comparingInt(o -> o.getWeight().orElse(0)))
                    .forEach(group -> {
                        if (Config.PREFIXGROUPS.contains(group.getName())) {
                            prefix.append("<white>[").append(group.getCachedData().getMetaData().getPrefix()).append("]</white>");
                        }
                    });
        }
        LegacyComponentSerializer.builder().character('&').hexColors();
        prefix.append("<white>[").append(user.getCachedData().getMetaData().getPrefix()).append("]</white>");

        return prefix.toString();
    }

    // @teri you don't reference the plugin instance from the API instance, this creates a circular reference and breaks on compile and will never run
    public static String getStaffPrefix(UUID uuid) {
        StringBuilder prefix = new StringBuilder();
        LuckPerms luckPerms = VelocityChat.getPlugin().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        if(user == null) return prefix.toString();
        if(user.getCachedData().getPermissionData().checkPermission("group." + Config.MINIMIUMSTAFFRANK).asBoolean()) {
            prefix.append("<white>[").append(user.getCachedData().getMetaData().getPrefix()).append("]</white>");
        }
        return prefix.toString();
    }

    public static String getDisplayName(UUID uuid) {
        StringBuilder prefix = new StringBuilder();
        LuckPerms luckPerms = VelocityChat.getPlugin().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        if(user == null) return "";
        return user.getUsername();
    }

}
