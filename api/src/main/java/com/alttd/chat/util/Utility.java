package com.alttd.chat.util;

import com.alttd.chat.ChatAPI;
import com.alttd.chat.ChatImplementation;
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
        LuckPerms luckPerms = ChatAPI.get().getLuckPerms();
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
        LuckPerms luckPerms = ChatAPI.get().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        if(user == null) return "";
        if(!Config.STAFFGROUPS.contains(user.getPrimaryGroup())) return "";
        prefix.append("<white>[").append(user.getCachedData().getMetaData().getPrefix()).append("]</white>");

        return prefix.toString();
    }

    public static String getDisplayName(UUID uuid) {
        // todo add a <T> PlayerWrapper<T> @Destro
        /*ProxyServer proxy = ChatPlugin.getPlugin().getProxy();
        if (proxy.getPlayer(uuid).isEmpty()) return "";
        Player player = proxy.getPlayer(uuid).get();
        return player.getUsername();*/
        return "";
    }

}
