package com.alttd.chat.listeners;

import com.alttd.chat.config.Config;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatFilter;
import com.alttd.chat.util.Utility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class BookListener implements Listener {

    private final PlainTextComponentSerializer plainTextComponentSerializer = PlainTextComponentSerializer.plainText();

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerEditBookEvent(PlayerEditBookEvent event) {
        BookMeta bookMeta = event.getNewBookMeta();
        List<Component> pages = new ArrayList<>();
        for (Component component : bookMeta.pages()) {
            Component formatComponent = Component.text("%message%");
            Component message = parseMessageContent(event.getPlayer(), plainTextComponentSerializer.serialize(component));
            pages.add(formatComponent.replaceText(TextReplacementConfig.builder().match("%message%").replacement(message).build()));
        }
        bookMeta.pages(pages);
        event.setNewBookMeta(bookMeta);
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
        return component;

    }

}