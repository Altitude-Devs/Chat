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
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Stack;
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

    private final HashMap<UUID, Stack<Instant>> sendPlayerDeaths = new HashMap<>();
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerDeath(@NotNull PlayerDeathEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Stack<Instant> playerDeathsStack = sendPlayerDeaths.computeIfAbsent(uuid, key -> new Stack<>());
        Instant cutOff = Instant.now().minus(Config.DEATH_MESSAGES_LIMIT_PERIOD_MINUTES, ChronoUnit.MINUTES);

        while (!playerDeathsStack.isEmpty() && playerDeathsStack.peek().isBefore(cutOff)) {
            playerDeathsStack.pop();
        }

        if (playerDeathsStack.size() > Config.DEATH_MESSAGES_MAX_PER_PERIOD || serverConfig.MUTED) {
            event.deathMessage(Component.empty());
            return;
        }
            Component component = event.deathMessage();

        playerDeathsStack.push(Instant.now());
        if (component == null) {
            return;
        }
        TextReplacementConfig playerReplacement = TextReplacementConfig.builder()
                .match(event.getPlayer().getName())
                .replacement(event.getPlayer().displayName())
                .build();
        component = component.replaceText(playerReplacement);
        Player killer = event.getPlayer().getKiller();
        if (killer != null) {
            TextReplacementConfig killerReplacement = TextReplacementConfig.builder()
                    .match(killer.getName())
                    .replacement(killer.displayName())
                    .build();
            component = component.replaceText(killerReplacement);
        }
        component = MiniMessage.miniMessage().deserialize("<dark_red>[</dark_red><red>☠</red><dark_red>]</dark_red> ").append(component);
        component = component.style(Style.style(TextColor.color(255, 155, 48), TextDecoration.ITALIC));
        event.deathMessage(component);
    }

}
