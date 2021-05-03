package com.alttd.chat.handlers;

import com.alttd.chat.config.Config;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.*;

public class ChatHandler {

    private List<ChatPlayer> chatPlayers;

    public ChatHandler() {
        chatPlayers = new ArrayList<>();
    }

    public void addPlayer(ChatPlayer chatPlayer) {
        chatPlayers.add(chatPlayer);
    }

    public void removePlayer(ChatPlayer chatPlayer) {
        if(chatPlayer != null)
            chatPlayers.remove(chatPlayer);
    }

    public void removePlayer(UUID uuid) {
        removePlayer(getChatPlayer(uuid));
    }

    public ChatPlayer getChatPlayer(UUID uuid) {
        for(ChatPlayer p: chatPlayers) {
            if(p.getUuid() == uuid)
                return p;
        }
        return null;
    }

    public List<ChatPlayer> getChatPlayers() {
        return Collections.unmodifiableList(chatPlayers);
    }

    public void globalChat(CommandSource source, String message) {
        String senderName, serverName;
        Map<String, String> map = new HashMap<>();

        if (source instanceof Player) {
            Player sender = (Player) source;
            senderName = sender.getUsername();
            serverName = sender.getCurrentServer().isPresent() ? sender.getCurrentServer().get().getServerInfo().getName() : "Altitude";
        } else {
            senderName = "Console"; // TODO console name from config
            serverName = "Altitude";
        }
        map.put("sender", senderName);
        map.put("message", message);
        map.put("server", serverName);

        for(ChatPlayer p: chatPlayers) {
            if(p.isGlobalChatEnabled());
                p.getPlayer().sendMessage(MiniMessage.get().parse(Config.GCFORMAT, map));
                //TODO send global chat with format from config
        }
    }
}
