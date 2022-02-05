package com.alttd.chat.listeners;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.handler.ChatHandler;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.util.GalaxyUtility;
import com.alttd.chat.util.Utility;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.placeholder.Replacement;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class ChatListener implements Listener, ChatRenderer {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {

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

        message = RegexManager.replaceText(player.getName(), player.getUniqueId(), message); // todo a better way for this
        if(message == null) {
            event.setCancelled(true);
            GalaxyUtility.sendBlockedNotification("Language", player, input, "");
            return; // the message was blocked
        }

        if(!player.hasPermission("chat.format")) {
            message = Utility.stripTokens(message);
        } else {
            message = Utility.parseColors(message);
        }

        if(message.contains("[i]"))
            message = message.replace("[i]", "<[i]>"); // end of todo

        message = Utility.formatText(message);

        Map<String, Replacement<?>> placeholders = new HashMap<>();
        placeholders.put("message", Replacement.miniMessage(message));
        placeholders.put("[i]", Replacement.component(ChatHandler.itemComponent(player.getInventory().getItemInMainHand())));

        Component component = Utility.parseMiniMessage("<message>", placeholders);

        event.message(component);
        event.renderer(this);
    }

    @Override
    public @NotNull Component render(@NotNull Player player, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());

        Map<String, Replacement<?>> placeholders = new HashMap<>();
        placeholders.put("sender", Replacement.component(user.getDisplayName()));
        placeholders.put("sendername", Replacement.component(player.name()));
        placeholders.put("prefix", Replacement.component(user.getPrefix()));
        placeholders.put("prefixall", Replacement.component(user.getPrefixAll()));
        placeholders.put("staffprefix", Replacement.component(user.getStaffPrefix()));
        placeholders.put("message", Replacement.component(message));

        return Utility.parseMiniMessage(Config.CHATFORMAT, placeholders);
    }

}