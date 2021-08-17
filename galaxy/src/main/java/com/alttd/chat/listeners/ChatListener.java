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
import litebans.api.Database;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChatListener implements Listener, ChatRenderer {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) {

        if (ChatPlugin.getInstance().serverMuted() && !event.getPlayer().hasPermission("chat.bypass-server-muted")) {
            event.setCancelled(true);

            Player player = event.getPlayer();
            MiniMessage miniMessage = MiniMessage.get();
            Component blockedNotification = miniMessage.parse("<red>[Chat Muted] "
                    + Utility.getDisplayName(player.getUniqueId(), player.getName())
                    + " tried to say: "
                    + PlainComponentSerializer.plain().serialize(event.message()) + "</red>");

            Bukkit.getOnlinePlayers().forEach(a ->{
                if (a.hasPermission("chat.alert-blocked")) {
                    a.sendMessage(blockedNotification);//TODO make configurable (along with all the messages)
                }
            });
            return;
        }

        Player player = event.getPlayer();
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());

        event.viewers().removeIf(audience -> audience instanceof Player receiver
                && ChatUserManager.getChatUser(receiver.getUniqueId()).getIgnoredPlayers().contains(player.getUniqueId()));

        Component input = event.message();
        String message = PlainComponentSerializer.plain().serialize(input);

        MiniMessage miniMessage = MiniMessage.get();

        message = RegexManager.replaceText(player.getName(), player.getUniqueId(), message); // todo a better way for this
        if(message == null) {
            event.setCancelled(true);
            GalaxyUtility.sendBlockedNotification("Language", player, input, "");
            return; // the message was blocked
        }


        if(!player.hasPermission("chat.format")) {
            message = miniMessage.stripTokens(message);
        } else {
            message = Utility.parseColors(message);
        }

        if(message.contains("[i]"))
            message = message.replace("[i]", "<[i]>"); // end of todo

        message = Utility.formatText(message);

        List<Template> templates = new ArrayList<>(List.of(
                Template.of("message", message),
                Template.of("[i]", ChatHandler.itemComponent(player.getInventory().getItemInMainHand()))
        ));

        Component component = miniMessage.parse("<message>", templates);

        event.message(component);
        event.renderer(this);
    }

    @Override
    public @NotNull Component render(@NotNull Player player, @NotNull Component sourceDisplayName, @NotNull Component message, @NotNull Audience viewer) {
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());

        List<Template> templates = new ArrayList<>(List.of(
                Template.of("sender", user.getDisplayName()),
                Template.of("sendername", player.getName()),
                Template.of("prefix", user.getPrefix()),
                Template.of("prefixall", user.getPrefixAll()),
                Template.of("staffprefix", user.getStaffPrefix()),
                Template.of("message", message)
        ));

        return MiniMessage.get().parse(Config.CHATFORMAT, templates);
    }

}