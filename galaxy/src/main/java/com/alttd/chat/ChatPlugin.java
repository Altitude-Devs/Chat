package com.alttd.chat;

import com.alttd.chat.commands.GlobalChat;
import com.alttd.chat.config.Config;
import com.alttd.chat.handler.ChatHandler;
import com.alttd.chat.listeners.PlayerListener;
import com.alttd.chat.listeners.PluginMessage;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatPlugin extends JavaPlugin {

    private static ChatPlugin instance;

    private ChatAPI chatAPI;
    private ChatHandler chatHandler;

    private String messageChannel;

    @Override
    public void onEnable() {
        instance = this;
        chatAPI = new ChatImplementation();
        chatHandler = new ChatHandler();
        registerListener(new PlayerListener());
        registerCommand("globalchat", new GlobalChat());

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

    public void registerCommand(String commandName, CommandExecutor CommandExecutor) {
        getCommand(commandName).setExecutor(CommandExecutor);
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
}
