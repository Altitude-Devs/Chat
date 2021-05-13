package com.alttd.velocitychat.listeners;

import com.alttd.velocitychat.ChatPlugin;
import com.alttd.chat.objects.ChatPlayer;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;

public class PlayerListener {

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerLogin(LoginEvent event) {
        ChatPlugin.getPlugin().getChatHandler().addPlayer(new ChatPlayer(event.getPlayer().getUniqueId()));
    }

    @Subscribe
    public void quitEvent(DisconnectEvent event) {
        ChatPlugin.getPlugin().getChatHandler().removePlayer(event.getPlayer().getUniqueId());
    }
}
