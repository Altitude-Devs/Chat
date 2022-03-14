package com.alttd.chat.listeners;

import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.ModifiableString;
import com.alttd.chat.util.GalaxyUtility;
import com.alttd.chat.util.Utility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
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
        ChatUser chatUser = new ChatUser(uuid, -1, null);
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
                String message = PlainTextComponentSerializer.plainText().serialize(component);
                ModifiableString modifiableString = new ModifiableString(message);

                Player player = event.getPlayer();

                // todo a better way for this
                if (!RegexManager.filterText(player.getName(), player.getUniqueId(), modifiableString, false, "sign")) {
                    GalaxyUtility.sendBlockedNotification("Sign Language",
                            player,
                            Utility.parseMiniMessage(Utility.parseColors(modifiableString.string())),
                            "");
                }
                message = modifiableString.string();

                component = message == null ? Component.empty() : Component.text(message);

                event.line(i, component);
            }
        }
    }

}
