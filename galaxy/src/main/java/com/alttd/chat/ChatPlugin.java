package com.alttd.chat;

import com.alttd.chat.commands.*;
import com.alttd.chat.config.Config;
import com.alttd.chat.config.ServerConfig;
import com.alttd.chat.database.DatabaseConnection;
import com.alttd.chat.handler.ChatHandler;
import com.alttd.chat.listeners.ChatListener;
import com.alttd.chat.listeners.PlayerListener;
import com.alttd.chat.listeners.PluginMessage;
import com.alttd.chat.objects.channels.Channel;
import com.alttd.chat.objects.channels.CustomChannel;
import com.alttd.chat.util.ALogger;
import com.alttd.chat.util.Utility;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class ChatPlugin extends JavaPlugin {

    private static ChatPlugin instance;

    private ChatAPI chatAPI;
    private ChatHandler chatHandler;

    private String messageChannel;
    private ServerConfig serverConfig;

    @Override
    public void onEnable() {
        instance = this;
        ALogger.init(getSLF4JLogger());
        chatAPI = new ChatImplementation();
        chatHandler = new ChatHandler();
        DatabaseConnection.initialize();
        serverConfig = new ServerConfig(Bukkit.getServerName());
        registerListener(new PlayerListener(), new ChatListener());
        if(serverConfig.GLOBALCHAT) {
            registerCommand("globalchat", new GlobalChat());
            registerCommand("toggleglobalchat", new ToggleGlobalChat());
        }
        registerCommand("message", new Message());
        registerCommand("reply", new Reply());
        registerCommand("continue", new Continue());
        registerCommand("ignore", new Ignore());
        registerCommand("unignore", new Unignore());
        registerCommand("muteserver", new MuteServer());
        registerCommand("spy", new Spy());
        registerCommand("chatclear", new ChatClear());
//        registerCommand("chatparty", new ChatParty());
        registerCommand("p", new PartyChat());
        for (Channel channel : Channel.getChannels()) {
            if (!(channel instanceof CustomChannel customChannel)) continue;
           this.getServer().getCommandMap().register(channel.getChannelName().toLowerCase(), new ChatChannel(customChannel));
        }

        messageChannel = Config.MESSAGECHANNEL;
        getServer().getMessenger().registerOutgoingPluginChannel(this, messageChannel);
        getServer().getMessenger().registerIncomingPluginChannel(this, messageChannel, new PluginMessage());
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public void registerListener(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    public void registerCommand(String commandName, CommandExecutor commandExecutor) {
        getCommand(commandName).setExecutor(commandExecutor);
    }

    public void registerCommand(String commandName, CommandExecutor commandExecutor, List<String> aliases) {
        PluginCommand command = getCommand(commandName);
        command.setAliases(aliases);
        command.setExecutor(commandExecutor);
    }

    public static ChatPlugin getInstance() {
        return instance;
    }

    public ChatAPI getChatAPI() {
        return chatAPI;
    }

    public ChatHandler getChatHandler() {
        return chatHandler;
    }

    public boolean serverGlobalChatEnabled() {
        return serverConfig.GLOBALCHAT;
    }

    public boolean serverMuted() {
        return serverConfig.MUTED;
    }

    public void toggleServerMuted() {
        serverConfig.MUTED = !serverConfig.MUTED;
    }

    public void ReloadConfig() {
        chatAPI.ReloadConfig();
        chatAPI.ReloadChatFilters();
        serverConfig = new ServerConfig(Bukkit.getServerName());
        Bukkit.broadcast(Utility.parseMiniMessage("Reloaded ChatPlugin Config."), "command.chat.reloadchat");
        ALogger.info("Reloaded ChatPlugin config.");
    }
}
