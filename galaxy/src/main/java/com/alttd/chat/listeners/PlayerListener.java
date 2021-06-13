package com.alttd.chat.listeners;

import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.UUID;

public class PlayerListener implements Listener {

    @EventHandler
    private void onPlayerLogin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        ChatUser user = ChatUserManager.getChatUser(uuid);
        if(user != null) return;

        // todo actually load the users from db
        ChatUserManager.addUser(new ChatUser(uuid, -1, false, false));

    }


}
