package com.alttd.chat.handlers;

import com.alttd.chat.api.PrivateMessageEvent;
import com.alttd.chat.config.Config;
import com.alttd.chat.objects.ChatUser;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;

public class ChatHandler {

    public void privateMessage(PrivateMessageEvent event) {
        String senderName;
        String receiverName;
        CommandSource commandSource = event.getSender();
        if (commandSource instanceof Player) {
            Player sender = (Player) event.getSender();
            senderName = sender.getUsername();
            //plugin.getChatHandler().getChatPlayer(sender.getUniqueId()).setReplyTarget(event.getRecipient().getUniqueId()); // TODO this needs to be cleaner
        } else {
            senderName = Config.CONSOLENAME;
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
