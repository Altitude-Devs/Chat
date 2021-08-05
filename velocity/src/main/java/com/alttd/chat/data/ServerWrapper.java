package com.alttd.chat.data;

import com.alttd.chat.config.ServerConfig;
import com.alttd.chat.managers.ChatUserManager;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;

import java.util.UUID;

public class ServerWrapper {

    private final RegisteredServer registeredServer;
    private final String serverName;

    private final boolean globalChat;
    private final boolean joinMessages;

    public ServerWrapper(RegisteredServer registeredServer, ServerConfig serverConfig) {
        this.registeredServer = registeredServer;
        this.serverName = registeredServer.getServerInfo().getName();

        this.globalChat = serverConfig.GLOBALCHAT;
        this.joinMessages = serverConfig.JOINLEAVEMSSAGES;
    }

    public RegisteredServer getRegisteredServer() {
        return registeredServer;
    }

    public String serverName() {
        return serverName;
    }

    public boolean globalChat()
    {
        return globalChat;
    }

    public boolean joinMessages() {
        return joinMessages;
    }

    public void sendJoinLeaveMessage(UUID uuid, Component component) {
        if(joinMessages())
            getRegisteredServer().getPlayersConnected().stream()
                    .filter(p -> !ChatUserManager.getChatUser(p.getUniqueId()).getIgnoredPlayers().contains(uuid))
                    .forEach(p -> p.sendMessage(component));
    }
}
