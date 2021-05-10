package com.alttd.chat;

import com.alttd.chat.commands.GlobalAdminChat;
import com.alttd.chat.commands.GlobalChat;
import com.alttd.chat.commands.GlobalChatToggle;
import com.alttd.chat.config.Config;
import com.alttd.chat.database.DatabaseConnection;
import com.alttd.chat.handlers.ChatHandler;
import com.alttd.chat.listeners.ChatListener;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

@Plugin(id = "chatplugin", name = "ChatPlugin", version = "1.0.0",
        description = "A chat plugin for Altitude Minecraft Server",
        authors = {"destro174", "teri"},
        dependencies = {@Dependency(id = "luckperms")}
        )
public class ChatPlugin {

    private static ChatPlugin plugin;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;

    private ChatAPI chatAPI;
    private DatabaseConnection databaseConnection;
    private ChatHandler chatHandler;

    @Inject
    public ChatPlugin(ProxyServer proxyServer, Logger proxyLogger, @DataDirectory Path proxydataDirectory) {
        plugin = this;
        server = proxyServer;
        logger = proxyLogger;
        dataDirectory = proxydataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Config.init(getDataDirectory());
        loadCommands();
        chatAPI = new ChatImplementation();
        databaseConnection = chatAPI.getDataBase();
        if (!databaseConnection.initialize()) {
            // todo should we do this in the API or in the implementation?
            return;
        }
        chatHandler = new ChatHandler();
        server.getEventManager().register(this, new ChatListener());
    }

    public File getDataDirectory() {
        return dataDirectory.toFile();
    }

    public static ChatPlugin getPlugin() {
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
        new GlobalChatToggle(server);
        new GlobalChat(server);
        // all commands go here
    }

    public ChatAPI API() {
        return chatAPI;
    }



    public ChatHandler getChatHandler() {
        return chatHandler;
    }
}
