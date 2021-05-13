package com.alttd.chat.handler;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class ChatHandler {

    private ChatPlugin plugin;

    public ChatHandler() {
        plugin = ChatPlugin.getInstance();
    }

    public void globalChat(CommandSender source, String message) {
        String senderName, prefix = "";
        Map<String, String> map = new HashMap<>();

        if (source instanceof Player) {
            Player sender = (Player) source;
            senderName = sender.getDisplayName();
            prefix = plugin.getChatAPI().getPrefix(sender.getUniqueId());
        } else {
            senderName = Config.CONSOLENAME;
        }

        MiniMessage miniMessage = MiniMessage.get();
        if(!source.hasPermission("chat.format"))
            message = miniMessage.stripTokens(message);

        map.put("sender", senderName);
        map.put("message", message);
        map.put("server", Bukkit.getServerName());
        map.put("prefix", prefix);

        Component component = miniMessage.parse(Config.GCFORMAT, map);

        Bukkit.broadcast(component, Config.GCPERMISSION);
        // TODO this should be a plugin message, so proxy can handle the forwarding, we only do this on server level for [i] support

    }
}
