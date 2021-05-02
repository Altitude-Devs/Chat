package com.alttd.chat;

import com.alttd.chat.config.Config;
import com.alttd.chat.listeners.ChatListener;
import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import org.slf4j.Logger;

import java.io.File;
import java.nio.file.Path;

@Plugin(id = "chatplugin", name = "ChatPlugin", version = "1.0.0",
        description = "A chat plugin for Altitude Minecraft Server",
        authors = {"destro174", "teri"}
        )
public class ChatPlugin {

    private static ChatPlugin plugin;
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataDirectory;
    private LuckPerms luckPerms;

    @Inject
    public ChatPlugin(ProxyServer proxyServer, Logger proxylogger, @DataDirectory Path proxydataDirectory) {
        plugin = this;
        server = proxyServer;
        logger = proxylogger;
        dataDirectory = proxydataDirectory;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        Config.init(getDataDirectory());
        loadCommands();
        server.getEventManager().register(this, new ChatListener());
        //statusTask = new StatusTask();
        //statusTask.init();
    }

    public File getDataDirectory() {
        return dataDirectory.toFile();
    }

    public static ChatPlugin getInstance() {
        return plugin;
    }

    public LuckPerms getLuckPerms() {
        if(luckPerms == null)
            luckPerms = LuckPermsProvider.get();
        return luckPerms;
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getProxy() {
        return server;
    }

    public void loadCommands() {
        // all commands go here
    }
}
