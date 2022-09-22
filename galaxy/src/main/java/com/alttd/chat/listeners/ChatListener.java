package com.alttd.chat.listeners;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.handler.ChatHandler;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.ModifiableString;
import com.alttd.chat.objects.Toggleable;
import com.alttd.chat.util.ALogger;
import com.alttd.chat.util.GalaxyUtility;
import com.alttd.chat.util.Utility;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class ChatListener implements Listener {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        event.setCancelled(true); //Always cancel the event because we do not want to deal with Microsoft's stupid bans
        Toggleable toggleable = Toggleable.getToggleable(event.getPlayer().getUniqueId());
        if (toggleable != null) {
            toggleable.sendMessage(event.getPlayer(), event.message());
            return;
        }
        if (ChatPlugin.getInstance().serverMuted() && !event.getPlayer().hasPermission("chat.bypass-server-muted")) {
            Player player = event.getPlayer();
            GalaxyUtility.sendBlockedNotification("Chat Muted", player, event.message(), "");
            return;
        }

        Player player = event.getPlayer();
//        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());

        Set<Player> receivers = event.viewers().stream().filter(audience -> audience instanceof Player)
                .map(audience -> (Player) audience)
                .filter(receiver -> !ChatUserManager.getChatUser(receiver.getUniqueId()).getIgnoredPlayers().contains(player.getUniqueId()))
                .collect(Collectors.toSet());

        Component input = event.message();
        String message = PlainTextComponentSerializer.plainText().serialize(input);
        ModifiableString modifiableString = new ModifiableString(message);
         // todo a better way for this
        if(!RegexManager.filterText(player.getName(), player.getUniqueId(), modifiableString, "chat")) {
            event.setCancelled(true);
            GalaxyUtility.sendBlockedNotification("Language", player,
                    Utility.parseMiniMessage(Utility.parseColors(modifiableString.string())),
                    "");
            return; // the message was blocked
        }
        message = modifiableString.string();
        if(!player.hasPermission("chat.format")) {
            message = Utility.stripTokens(message);
        } else {
            message = Utility.parseColors(message);
        }

        Component component = Utility.parseMiniMessage(Utility.formatText(message));
        component = component.replaceText(
                TextReplacementConfig.builder()
                        .once()
                        .matchLiteral("[i]")
                        .replacement(ChatHandler.itemComponent(player.getInventory().getItemInMainHand()))
                        .build());

        component = render(player, component);
        for (Player receiver : receivers) {
            receiver.sendMessage(component);
        }
        ALogger.info(PlainTextComponentSerializer.plainText().serialize(component));
    }

    public @NotNull Component render(@NotNull Player player, @NotNull Component message) {
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.component("sender", user.getDisplayName()),
//                Placeholder.component("sendername", player.name()),
                Placeholder.component("prefix", user.getPrefix()),
                Placeholder.component("prefixall", user.getPrefixAll()),
                Placeholder.component("staffprefix", user.getStaffPrefix()),
                Placeholder.component("message", message)
        );

        return Utility.parseMiniMessage(Config.CHATFORMAT.replaceAll("<sendername>", player.getName()), placeholders);
    }

}