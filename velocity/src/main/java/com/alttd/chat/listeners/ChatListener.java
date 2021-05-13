package com.alttd.velocitychat.listeners;

import com.alttd.velocitychat.ChatPlugin;
import com.alttd.velocitychat.api.GlobalAdminChatEvent;
import com.alttd.velocitychat.api.PrivateMessageEvent;
import com.alttd.chat.config.Config;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;
// TODO code CLEANUP
public class ChatListener {

    private ChatPlugin plugin;

    public ChatListener() {
        plugin = ChatPlugin.getPlugin();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onMessage(PrivateMessageEvent event) {
        // TODO check muted, etc
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onGlobalStaffChat(GlobalAdminChatEvent event) {
        String senderName;
        String serverName;
        CommandSource commandSource = event.getSender();
        if (commandSource instanceof Player) {
            Player sender = (Player) event.getSender();
            senderName = sender.getUsername();
            serverName = sender.getCurrentServer().isPresent() ? sender.getCurrentServer().get().getServerInfo().getName() : "Altitude";
        } else {
            senderName = "Console"; // TODO console name from config
            serverName = "Proxy";
        }

        MiniMessage miniMessage = MiniMessage.get();

        Map<String, String> map = new HashMap<>();

        map.put("sender", senderName);
        map.put("message", event.getMessage());
        map.put("server", serverName);

        Component message = miniMessage.parse(Config.GACFORMAT, map);

        plugin.getProxy().getAllPlayers().stream().filter(target -> target.hasPermission("command.proxy.globaladminchat")).forEach(target -> {
            target.sendMessage(message);
        });
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerChat(PlayerChatEvent event) {
        // do stuff
    }


}
