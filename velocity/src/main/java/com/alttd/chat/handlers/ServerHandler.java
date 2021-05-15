package com.alttd.chat.handlers;

import com.alttd.chat.VelocityChat;
import com.alttd.chat.config.ServerConfig;
import com.alttd.chat.data.ServerWrapper;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ServerHandler {

    private VelocityChat plugin;

    private static List<ServerWrapper> servers;

    public ServerHandler() {
        plugin = VelocityChat.getPlugin();
        initialize();
    }

    public void cleanup() { // for use on /reload?
        servers.clear();
        initialize();
    }

    public void initialize() {
        servers = new ArrayList<>();

        for (RegisteredServer registeredServer : plugin.getProxy().getAllServers()) {
            servers.add(new ServerWrapper(registeredServer, new ServerConfig(registeredServer.getServerInfo().getName())));
        }
    }

    public void sendGlobalChat(String message) {
        servers.stream()
                .filter(serverWrapper -> serverWrapper.globalChat())
                .forEach(serverWrapper -> serverWrapper.getRegisteredServer().sendMessage(MiniMessage.get().parse(message)));
    }

    public List<ServerWrapper> getServers()
    {
        return Collections.unmodifiableList(servers);
    }

    public ServerWrapper getWrapper(String serverName) {
        for(ServerWrapper wrapper : getServers()) {
            if(wrapper.serverName().equalsIgnoreCase(serverName)) {
                return wrapper;
            }
        }
        return null;
    }
}
