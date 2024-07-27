package com.alttd.chat.listeners;

import com.alttd.chat.objects.chat_log.ChatLogHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.Plugin;

public class ShutdownListener implements Listener {

    private final ChatLogHandler chatLogHandler;
    private final Plugin thisPlugin;

    public ShutdownListener(ChatLogHandler chatLogHandler, Plugin thisPlugin) {
        this.chatLogHandler = chatLogHandler;
        this.thisPlugin = thisPlugin;
    }

    @EventHandler
    public void onShutdown(PluginDisableEvent event) {
        if (!event.getPlugin().getName().equals(thisPlugin.getName())){
            return;
        }
        chatLogHandler.shutDown();
    }

}
