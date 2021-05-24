package com.alttd.chat;

import com.alttd.chat.commands.GlobalAdminChat;
import com.alttd.chat.commands.GlobalChat;
import com.alttd.chat.config.Config;
import com.alttd.chat.handlers.ChatHandler;
import com.alttd.chat.handlers.ServerHandler;
import com.alttd.chat.listeners.ChatListener;
import com.alttd.chat.listeners.ProxyPlayerListener;
import com.alttd.chat.listeners.PluginMessageListener;
import com.alttd.chat.util.ALogger;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

@Plugin(id = "chatplugin", name = "ChatPlugin", version = "1.0.0",
        description = "A chat plugin for Altitude Minecraft Server",
        authors = {"destro174", "teri"},
        dependencies = {@Dependency(id = "luckperms")}
        )
public class VelocityChat {

    private static VelocityChat plugin;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private ChatAPI chatAPI;
    private ChatHandler chatHandler;
    private ServerHandler serverHandler;

    private ChannelIdentifier channelIdentifier;

    @Inject
    public VelocityChat(ProxyServer proxyServer, Logger proxyLogger, @DataDirectory Path proxydataDirectory) {
        plugin = this;
        server = proxyServer;
        logger = proxyLogger;
        dataDirectory = proxydataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        new ALogger(logger);
        chatAPI = new ChatImplementation(getDataDirectory());

        serverHandler = new ServerHandler();
        chatHandler = new ChatHandler();
        server.getEventManager().register(this, new ChatListener());
        server.getEventManager().register(this, new ProxyPlayerListener());
        String[] channels = Config.MESSAGECHANNEL.split(":");// todo add a check for this?
        channelIdentifier = MinecraftChannelIdentifier.create(channels[0], channels[1]);
        server.getChannelRegistrar().register(channelIdentifier);
        server.getEventManager().register(this, new PluginMessageListener(channelIdentifier));

        loadCommands();
    }

    public File getDataDirectory() {
        return dataDirectory.toFile();
    }

    public static VelocityChat getPlugin() {
        return plugin;
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getProxy() {
        return server;
    }

    public void loadCommands() {
        new GlobalAdminChat(server);
        new GlobalChat(server);
        // all (proxy)commands go here
    }

    public ChatAPI API() {
        return chatAPI;
    }

    public ChatHandler getChatHandler() {
        return chatHandler;
    }

    public ServerHandler getServerHandler() {
        return serverHandler;
    }
}
