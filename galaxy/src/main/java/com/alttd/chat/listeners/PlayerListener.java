package com.alttd.chat.listeners;

import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.util.GalaxyUtility;
import com.alttd.chat.util.Utility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerListener implements Listener {

    @EventHandler
    private void onPlayerLogin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        ChatUser user = ChatUserManager.getChatUser(uuid);
        if(user != null) return;

        // user failed to load - create a new one
        ChatUser chatUser = new ChatUser(uuid, -1, false, false);
        ChatUserManager.addUser(chatUser);
        Queries.saveUser(chatUser);

        //TODO load player on other servers with plugin message?
    }

    @EventHandler
    private void onPlayerLogout(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        ChatUser user = ChatUserManager.getChatUser(uuid);
        ChatUserManager.removeUser(user);
    }


    @EventHandler(ignoreCancelled = true) // untested
    public void onSignChangeE(SignChangeEvent event) {
        for (int i = 0; i < 4; i++) {
            Component component = event.line(i);
            if (component != null) {
                String message = PlainComponentSerializer.plain().serialize(component);
                Player player = event.getPlayer();
                message = RegexManager.replaceText(player.getName(), player.getUniqueId(), message); // todo a better way for this

                if (message == null) {
                    GalaxyUtility.sendBlockedNotification("Sign Language" , player, PlainComponentSerializer.plain().serialize(component), "");
                }

                component = message == null ? Component.empty() : Component.text(message);

                event.line(i, component);
            }
        }
    }

}
