package com.alttd.velocitychat.handlers;

import com.alttd.chat.database.Queries;
import com.alttd.chat.objects.ChatUser;
import com.alttd.velocitychat.VelocityChat;
import com.alttd.chat.config.ServerConfig;
import com.alttd.velocitychat.data.ServerWrapper;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.scheduler.ScheduledTask;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ServerHandler {

    private VelocityChat plugin;

    private static List<ServerWrapper> servers;
    private static Map<String, UUID> serverPlayers;
    public ScheduledTask cleanupTask; // add a better way to catch NULL uuid? early return? get from another source? ...

    public ServerHandler() {
        plugin = VelocityChat.getPlugin();
        serverPlayers = new TreeMap<>();
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

        cleanupTask = plugin.getProxy().getScheduler().buildTask(plugin, () -> {
            serverPlayers.values().removeIf(Objects::isNull);
        }).repeat(60, TimeUnit.SECONDS).schedule();

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

    public static UUID getPlayerUUID(String playerName) {
        return serverPlayers.computeIfAbsent(playerName, k -> Queries.getPlayerUUID(playerName));
    }

    public static void addPlayerUUID(String playerName, UUID uuid) {
        serverPlayers.putIfAbsent(playerName, uuid);
    }
}
