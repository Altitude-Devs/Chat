package com.alttd.chat.data;

import com.alttd.chat.config.ServerConfig;
import com.velocitypowered.api.proxy.server.RegisteredServer;

public class ServerWrapper
{
    private RegisteredServer registeredServer;

    private final boolean globalChat;

    public ServerWrapper(RegisteredServer registeredServer, ServerConfig serverConfig)
    {
        this.registeredServer = registeredServer;

        this.globalChat = serverConfig.GLOBALCHAT;

    }

    public RegisteredServer getRegisteredServer() {
        return registeredServer;
    }

    public boolean globalChat()
    {
        return globalChat;
    }

}
