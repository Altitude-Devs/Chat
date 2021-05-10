package com.alttd.chat.listeners;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.api.GlobalStaffChatEvent;
import com.alttd.chat.api.MessageEvent;
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
    public void onMessage(MessageEvent event) {
        String senderName;
        String receiverName;
        CommandSource commandSource = event.getSender();
        if (commandSource instanceof Player) {
            Player sender = (Player) event.getSender();
            senderName = sender.getUsername();
            plugin.getChatHandler().getChatPlayer(sender.getUniqueId()).setReplyTarget(event.getRecipient().getUniqueId()); // TODO this needs to be cleaner
        } else {
            senderName = "Console"; // TODO console name from config
        }
        receiverName = event.getRecipient().getUsername();

        MiniMessage miniMessage = MiniMessage.get();

        Map<String, String> map = new HashMap<>();

        map.put("sender", senderName);
        map.put("receiver", receiverName);
        map.put("message", event.getMessage());
        map.put("server", event.getRecipient().getCurrentServer().isPresent() ? event.getRecipient().getCurrentServer().get().getServerInfo().getName() : "Altitude");

        Component senderMessage = miniMessage.parse(Config.MESSAGESENDER, map);
        Component receiverMessage = miniMessage.parse(Config.MESSAGERECIEVER, map);

        event.getSender().sendMessage(senderMessage);
        event.getRecipient().sendMessage(receiverMessage);
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onGlobalStaffChat(GlobalStaffChatEvent event) {
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
