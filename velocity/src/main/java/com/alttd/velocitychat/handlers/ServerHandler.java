package com.alttd.velocitychat.handlers;

import com.alttd.velocitychat.VelocityChat;
import com.alttd.chat.config.ServerConfig;
import com.alttd.velocitychat.data.ServerWrapper;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.server.RegisteredServer;

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

    public void sendGlobalChat(String uuid, String message) {
//        Component component = GsonComponentSerializer.gson().deserialize(message);

        servers.stream()
                .filter(ServerWrapper::globalChat)
                .map(ServerWrapper::getRegisteredServer)
                .forEach(registeredServer -> {
                    ByteArrayDataOutput buf = ByteStreams.newDataOutput();
                    buf.writeUTF("globalchat");
                    buf.writeUTF(uuid);
                    buf.writeUTF(message);
                    registeredServer.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), buf.toByteArray());
                });
//                .filter(serverWrapper -> serverWrapper.globalChat())
//                .forEach(serverWrapper -> serverWrapper.getRegisteredServer().sendMessage(component));
    }

    public List<ServerWrapper> getServers()
    {
        return Collections.unmodifiableList(servers);
    }

    public ServerWrapper getWrapper(String serverName) {
        for(ServerWrapper wrapper : servers) {
            if(wrapper.serverName().equalsIgnoreCase(serverName)) {
                return wrapper;
            }
        }
        return null;
    }
}
