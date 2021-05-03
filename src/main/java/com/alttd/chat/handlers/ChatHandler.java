package com.alttd.chat.handlers;

import com.velocitypowered.api.command.CommandSource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
        for(ChatPlayer p: chatPlayers) {
            if(p.isGlobalChatEnabled());
                //TODO send global chat with format from config
        }
    }
}
