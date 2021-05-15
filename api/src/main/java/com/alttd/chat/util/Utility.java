package com.alttd.chat.util;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;

import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;

public class Utility {

    public static String getPrefix(UUID uuid, boolean highest) {
        StringBuilder prefix = new StringBuilder();
        LuckPerms luckPerms = ChatPlugin.getPlugin().API().getLuckPerms();
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

    public static String getStaffPrefix(UUID uuid) {
        StringBuilder prefix = new StringBuilder();
        LuckPerms luckPerms = ChatPlugin.getPlugin().API().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        if(user == null) return "";
        if(!Config.STAFFGROUPS.contains(user.getPrimaryGroup())) return "";
        prefix.append("<white>[").append(user.getCachedData().getMetaData().getPrefix()).append("]</white>");

        return prefix.toString();
    }

    public static boolean checkPermission(UUID uuid, String permission) {
        ProxyServer proxy = ChatPlugin.getPlugin().getProxy();
        if (proxy.getPlayer(uuid).isEmpty()) return false;
        Player player = proxy.getPlayer(uuid).get();

        return player.hasPermission(permission);
    }

    public static String getDisplayName(UUID uuid) {
        ProxyServer proxy = ChatPlugin.getPlugin().getProxy();
        if (proxy.getPlayer(uuid).isEmpty()) return "";
        Player player = proxy.getPlayer(uuid).get();
        return player.getUsername();
    }

    public static void setPermission(UUID uuid, String permission, boolean toggleGc) {
        LuckPerms luckPerms = ChatPlugin.getPlugin().API().getLuckPerms();
        luckPerms.getUserManager().modifyUser(uuid, user -> {
            user.data().add(Node.builder(permission).value(toggleGc).build());
        });
    }
}
