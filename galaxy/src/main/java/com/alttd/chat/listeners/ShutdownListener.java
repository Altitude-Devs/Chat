package com.alttd.chat.listeners;

import com.alttd.chat.objects.chat_log.ChatLogHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;

public class ShutdownListener implements Listener {

    private final ChatLogHandler chatLogHandler;

    public ShutdownListener(ChatLogHandler chatLogHandler) {
        this.chatLogHandler = chatLogHandler;
    }

    @EventHandler
    public void onShutdown(PluginDisableEvent event) {
        chatLogHandler.shutDown();
    }

}
