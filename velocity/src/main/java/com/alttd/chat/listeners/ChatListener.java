package com.alttd.chat.listeners;

import com.alttd.chat.VelocityChat;
import com.alttd.chat.events.GlobalAdminChatEvent;
import com.alttd.chat.config.Config;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;
// TODO code CLEANUP
public class ChatListener {

    private VelocityChat plugin;

    public ChatListener() {
        plugin = VelocityChat.getPlugin();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onGlobalStaffChat(GlobalAdminChatEvent event) {
        String senderName = Config.CONSOLENAME;
        String serverName = "Altitude";
        CommandSource commandSource = event.getSender();
        if (commandSource instanceof Player) {
            Player sender = (Player) event.getSender();
            senderName = sender.getUsername();
            serverName = sender.getCurrentServer().isPresent() ? sender.getCurrentServer().get().getServerInfo().getName() : "Altitude";
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

}
