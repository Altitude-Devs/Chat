package com.alttd.chat.listeners;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.handler.ChatHandler;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.ModifiableString;
import com.alttd.chat.objects.Toggleable;
import com.alttd.chat.util.GalaxyUtility;
import com.alttd.chat.util.Utility;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import jdk.jshell.execution.Util;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

public class ChatListener implements Listener, ChatRenderer {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {
        Toggleable toggleable = Toggleable.getToggleable(event.getPlayer().getUniqueId());
        if (toggleable != null) {
            event.setCancelled(true);
            toggleable.sendMessage(event.getPlayer(), event.message());
            return;
        }
        if (ChatPlugin.getInstance().serverMuted() && !event.getPlayer().hasPermission("chat.bypass-server-muted")) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            GalaxyUtility.sendBlockedNotification("Chat Muted", player, event.message(), "");
            return;
        }

        Player player = event.getPlayer();
//        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());

        event.viewers().removeIf(audience -> audience instanceof Player receiver
                && ChatUserManager.getChatUser(receiver.getUniqueId()).getIgnoredPlayers().contains(player.getUniqueId()));

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
//        message = Utility.formatText(message);
//        TagResolver placeholders = TagResolver.resolver(
//                Placeholder.unparsed("message", message)
//        );
        component = component
//        Component component = Utility.parseMiniMessage("<message>", placeholders)
                .replaceText(TextReplacementConfig.builder().once().matchLiteral("[i]").replacement(ChatHandler.itemComponent(player.getInventory().getItemInMainHand())).build());

        event.message(component);
        event.renderer(this);
    }

    @Override
    public @NotNull Component render(@NotNull Player player, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.component("sender", user.getDisplayName()),
                Placeholder.component("sendername", player.name()),
                Placeholder.component("prefix", user.getPrefix()),
                Placeholder.component("prefixall", user.getPrefixAll()),
                Placeholder.component("staffprefix", user.getStaffPrefix()),
                Placeholder.component("message", message)
        );

        return Utility.parseMiniMessage(Config.CHATFORMAT, placeholders);
    }

}