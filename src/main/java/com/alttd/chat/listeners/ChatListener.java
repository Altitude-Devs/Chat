package com.alttd.chat.listeners;

import com.alttd.chat.api.MessageEvent;
import com.alttd.chat.config.Config;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;

public class ChatListener {

    @Subscribe
    public void onMessage(MessageEvent event) {
        String senderName;
        String receiverName;

        if (event.getSender() instanceof Player) {
            Player sender = (Player) event.getSender();
            senderName = sender.getUsername();
        } else {
            senderName = "UNKNOWN";
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
}
