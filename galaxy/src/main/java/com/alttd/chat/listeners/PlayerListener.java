package com.alttd.chat.listeners;

import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatUser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.List;
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

    @EventHandler(ignoreCancelled = true) // untested
    public void onSignChangeE(SignChangeEvent event) {
        for (int i = 0; i < 4; i++) {
            Component component = event.line(i);
            if (component != null) {
                String message = PlainComponentSerializer.plain().serialize(component);

                message = RegexManager.replaceText(message); // todo a better way for this

                component = message == null ? Component.empty() : Component.text(message);

                event.line(i, component);
            }
        }
    }

}
