package com.alttd.velocitychat;

import com.alttd.chat.ChatAPI;
import com.alttd.chat.ChatImplementation;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.velocitychat.commands.*;
import com.alttd.chat.config.Config;
import com.alttd.chat.database.DatabaseConnection;
import com.alttd.velocitychat.handlers.ChatHandler;
import com.alttd.velocitychat.handlers.ServerHandler;
import com.alttd.velocitychat.listeners.ChatListener;
import com.alttd.velocitychat.listeners.LiteBansListener;
import com.alttd.velocitychat.listeners.ProxyPlayerListener;
import com.alttd.velocitychat.listeners.PluginMessageListener;
import com.alttd.chat.util.ALogger;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
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
        dependencies = {@Dependency(id = "luckperms"), @Dependency(id = "litebans"), @Dependency(id = "proxydiscordlink")}
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
        ALogger.init(logger);

        chatAPI = new ChatImplementation();
        DatabaseConnection.initialize();
        PartyManager.initialize(); // load the parties from the db and add the previously loaded users to them

        serverHandler = new ServerHandler();
        chatHandler = new ChatHandler();
        server.getEventManager().register(this, new ChatListener());
        server.getEventManager().register(this, new ProxyPlayerListener());
        new LiteBansListener().init(); // init the litebans api listeners
        String[] channels = Config.MESSAGECHANNEL.split(":");// todo add a check for this?
        channelIdentifier = MinecraftChannelIdentifier.create(channels[0], channels[1]);
        server.getChannelRegistrar().register(channelIdentifier);
        server.getEventManager().register(this, new PluginMessageListener(channelIdentifier));
        loadCommands();
        // setup console chatuser
        ChatUser console = new ChatUser(Config.CONSOLEUUID, -1, null);
        console.setDisplayName(Config.CONSOLENAME);
        ChatUserManager.addUser(console);
    }

    public void ReloadConfig() {
        chatAPI.ReloadConfig();
        chatAPI.ReloadChatFilters();
        serverHandler.cleanup();
        ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeUTF("reloadconfig");
        ALogger.info("Reloaded ChatPlugin proxy config.");
        getProxy().getAllServers().stream().forEach(registeredServer -> registeredServer.sendPluginMessage(getChannelIdentifier(), buf.toByteArray()));
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
        new SilentJoinCommand(server);
        new GlobalAdminChat(server);
        new Reload(server);
        new MailCommand(server);
        new Report(server);
        server.getCommandManager().register("party", new PartyCommand());
        // all (proxy)commands go here
    }

    public ChatHandler getChatHandler() {
        return chatHandler;
    }

    public ServerHandler getServerHandler() {
        return serverHandler;
    }

    public ChannelIdentifier getChannelIdentifier() {
        return channelIdentifier;
    }
}
