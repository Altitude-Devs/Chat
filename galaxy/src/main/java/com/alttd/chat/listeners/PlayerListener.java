package com.alttd.chat.listeners;

import com.alttd.chat.config.Config;
import com.alttd.chat.config.ServerConfig;
import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.ModifiableString;
import com.alttd.chat.objects.Toggleable;
import com.alttd.chat.util.GalaxyUtility;
import com.alttd.chat.util.Utility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerListener implements Listener {

    private final ServerConfig serverConfig;

    public PlayerListener(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

    @EventHandler
    private void onPlayerLogin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        GalaxyUtility.addAdditionalChatCompletions(player);
        UUID uuid = player.getUniqueId();
        Toggleable.disableToggles(uuid);

        if (serverConfig.FIRST_JOIN_MESSAGES && System.currentTimeMillis() - player.getFirstPlayed() < TimeUnit.SECONDS.toMillis(10)) {
            player.getServer().sendMessage(MiniMessage.miniMessage().deserialize(Config.FIRST_JOIN, Placeholder.parsed("player", player.getName())));
        }

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
                ModifiableString modifiableString = new ModifiableString(component);

                Player player = event.getPlayer();

                // todo a better way for this
                if (!RegexManager.filterText(player.getName(), player.getUniqueId(), modifiableString, false, "sign")) {
                    GalaxyUtility.sendBlockedNotification("Sign Language",
                            player,
                            Utility.parseMiniMessage(Utility.parseColors(modifiableString.string())),
                            "");
                }

                component = modifiableString.component() == null ? Component.empty() : modifiableString.component();

                event.line(i, component);
            }
        }
    }

}
