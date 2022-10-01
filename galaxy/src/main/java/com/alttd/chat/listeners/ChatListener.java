package com.alttd.chat.listeners;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.handler.ChatHandler;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatFilter;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.ModifiableString;
import com.alttd.chat.objects.Toggleable;
import com.alttd.chat.util.ALogger;
import com.alttd.chat.util.GalaxyUtility;
import com.alttd.chat.util.Utility;
import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.stream.Collectors;

public class ChatListener implements Listener {

    private final PlainTextComponentSerializer plainTextComponentSerializer = PlainTextComponentSerializer.plainText();


    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChatCommandDecorate(AsyncChatCommandDecorateEvent event) {
        if (event.player() == null) return;

        Component formatComponent = Component.text("%message%");
        Component message = parseMessageContent(event.player(), plainTextComponentSerializer.serialize(event.originalMessage()));

        event.result(formatComponent.replaceText(TextReplacementConfig.builder().match("%message%").replacement(message).build()));
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onChatDecorate(AsyncChatDecorateEvent event) {
        if (event.player() == null) return;

        Component formatComponent = Component.text("%message%");
        Component message = parseMessageContent(event.player(), plainTextComponentSerializer.serialize(event.originalMessage()));

        event.result(formatComponent.replaceText(TextReplacementConfig.builder().match("%message%").replacement(message).build()));
    }

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

        input = render(player, plainTextComponentSerializer.deserialize(modifiableString.string()));
        for (Player receiver : receivers) {
            receiver.sendMessage(input);
        }
        ALogger.info(PlainTextComponentSerializer.plainText().serialize(input));
    }

    public @NotNull Component render(@NotNull Player player, @NotNull Component message) {
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.component("sender", user.getDisplayName()),
                Placeholder.parsed("sendername", player.getName()),
                Placeholder.component("prefix", user.getPrefix()),
                Placeholder.component("prefixall", user.getPrefixAll()),
                Placeholder.component("staffprefix", user.getStaffPrefix()),
                Placeholder.component("message", message)
        );

        return Utility.parseMiniMessage(Config.CHATFORMAT, placeholders);
    }

    private Component parseMessageContent(Player player, String rawMessage) {
        TagResolver.Builder tagResolver = TagResolver.builder();

        Utility.formattingPerms.forEach((perm, pair) -> {
            if (player.hasPermission(perm)) {
                tagResolver.resolver(pair.getX());
            }
        });

        MiniMessage miniMessage = MiniMessage.builder().tags(tagResolver.build()).build();
        Component component = miniMessage.deserialize(rawMessage);
        for(ChatFilter chatFilter :  RegexManager.getEmoteFilters()) {
            component = component.replaceText(
                    TextReplacementConfig.builder()
                            .times(Config.EMOTELIMIT)
                            .match(chatFilter.getRegex())
                            .replacement(chatFilter.getReplacement()).build());
        }

        component = component
                .replaceText(
                    TextReplacementConfig.builder()
                        .once()
                        .matchLiteral("[i]")
                        .replacement(ChatHandler.itemComponent(player.getInventory().getItemInMainHand()))
                        .build());

        return component;

    }

}