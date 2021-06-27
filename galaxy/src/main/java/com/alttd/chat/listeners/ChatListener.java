package com.alttd.chat.listeners;

import com.alttd.chat.config.Config;
import com.alttd.chat.handler.ChatHandler;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.util.Utility;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.plain.PlainComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ChatListener implements Listener, ChatRenderer {

    @EventHandler(ignoreCancelled = true)
    public void onPlayerChat(AsyncChatEvent event) { // this should also include a way to prevent a player from seeing chat
                                                        // @teri what about mutes?
        Player player = event.getPlayer();
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());

        // tweak this to make this slightly better:/
        event.viewers().removeIf(audience -> audience instanceof Player
                && user.getIgnoredPlayers().contains(((Player) audience).getUniqueId()));
/*        Set<Audience> viewers = event.viewers();
        Set<Audience> ignores = new HashSet<>();
        for(Audience audience : viewers) { // I don't like this setup, might alter this API to expose players...
            if(audience instanceof Player) { // the player option is removed in 1.17=/ bad paper devs
                UUID uuid = ((Player) audience).getUniqueId();
                if(user.getIgnoredPlayers().contains(uuid)) {
                    ignores.add(audience);
                }
            }
        }
        event.viewers().removeAll(ignores);*/

        Component input = event.message();
        String message = PlainComponentSerializer.plain().serialize(input);

        message = RegexManager.replaceText(message); // todo a better way for this
        if(message == null)  return; // the message was blocked

        MiniMessage miniMessage = MiniMessage.get();
        if(!player.hasPermission("chat.format")) {
            message = miniMessage.stripTokens(message);
        } else {
            message = Utility.parseColors(message);
        }

        if(message.contains("[i]"))
            message = message.replace("[i]", "<[i]>"); // end of todo

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
                Template.of("prefix", user.getPrefix()),
                Template.of("prefixall", user.getPrefixAll()),
                Template.of("staffprefix", user.getStaffPrefix()),
                Template.of("message", message)
        ));

        return MiniMessage.get().parse(Config.CHATFORMAT, templates);
    }

}