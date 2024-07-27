package com.alttd.chat.listeners;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.handler.ChatHandler;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.*;
import com.alttd.chat.objects.chat_log.ChatLogHandler;
import com.alttd.chat.util.ALogger;
import com.alttd.chat.util.GalaxyUtility;
import com.alttd.chat.util.Utility;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import io.papermc.paper.event.player.AsyncChatCommandDecorateEvent;
import io.papermc.paper.event.player.AsyncChatDecorateEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ChatListener implements Listener {

    private final PlainTextComponentSerializer plainTextComponentSerializer = PlainTextComponentSerializer.plainText();
    private final ChatLogHandler chatLogHandler;

    public ChatListener(ChatLogHandler chatLogHandler) {
        this.chatLogHandler = chatLogHandler;
    }


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

    private final Component mention = MiniMessage.miniMessage().deserialize(Config.MENTIONPLAYERTAG);
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

        Component input = event.message().colorIfAbsent(NamedTextColor.WHITE);

        ModifiableString modifiableString = new ModifiableString(input);

         // todo a better way for this
        if(!RegexManager.filterText(player.getName(), player.getUniqueId(), modifiableString, true, "chat", filterType -> {
            if (!filterType.equals(FilterType.PUNISH)) {
                ALogger.warn("Received another FilterType than punish when filtering chat and executing a filter action");
                return;
            }
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("punish");
            out.writeUTF(player.getName());
            out.writeUTF(player.getUniqueId().toString());
            out.writeUTF(modifiableString.string());
            player.sendPluginMessage(ChatPlugin.getInstance(), Config.MESSAGECHANNEL, out.toByteArray());
        })) {
            event.setCancelled(true);
            GalaxyUtility.sendBlockedNotification("Language", player,
                    modifiableString.component(),
                    "");
            chatLogHandler.addChatLog(player.getUniqueId(), player.getServer().getServerName(), PlainTextComponentSerializer.plainText().serialize(input), true);
            return; // the message was blocked
        }

        Set<Player> receivers = event.viewers().stream().filter(audience -> audience instanceof Player)
                .map(audience -> (Player) audience)
                .filter(receiver -> !ChatUserManager.getChatUser(receiver.getUniqueId()).getIgnoredPlayers().contains(player.getUniqueId()))
                .collect(Collectors.toSet());

        Set<Player> playersToPing = new HashSet<>();
        pingPlayers(playersToPing, modifiableString, player);

        input = render(player, modifiableString.component());
        for (Player receiver : receivers) {
            receiver.sendMessage(input);
        }
        for (Player pingPlayer : playersToPing) {
            pingPlayer.playSound(pingPlayer.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
        chatLogHandler.addChatLog(player.getUniqueId(), player.getServer().getServerName(), modifiableString.string(), false);
        ALogger.info(PlainTextComponentSerializer.plainText().serialize(input));
    }

    private void pingPlayers(Set<Player> playersToPing, ModifiableString modifiableString, Player player) {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            String name = onlinePlayer.getName();
            String nickName = PlainTextComponentSerializer.plainText().serialize(onlinePlayer.displayName());

            Pattern namePattern = Pattern.compile("\\b(?<!\\\\)" + name + "\\b", Pattern.CASE_INSENSITIVE);
//            Pattern escapedNamePattern = Pattern.compile("\\b\\\\" + name + "\\b", Pattern.CASE_INSENSITIVE);
            Pattern nickPattern = Pattern.compile("\\b(?<!\\\\)" + nickName + "\\b", Pattern.CASE_INSENSITIVE);
//            Pattern escapedNickPattern = Pattern.compile("\\b\\\\" + nickName + "\\b", Pattern.CASE_INSENSITIVE);

            if (namePattern.matcher(modifiableString.string()).find()) {
                modifiableString.replace(TextReplacementConfig.builder()
                        .once()
                        .match(namePattern)
                        .replacement(mention.append(onlinePlayer.displayName()))
                        .build());
                //TODO replace all instances of \name with just name but using the match result so the capitalization doesn't change
//                modifiableString.replace(TextReplacementConfig.builder()
//                        .once()
//                        .match(escapedNamePattern)
//                        .replacement((a, b) -> {
//                            String substring = a.group().substring(1);
//                            return ;
//                        });
                if (!ChatUserManager.getChatUser(onlinePlayer.getUniqueId()).getIgnoredPlayers().contains(player.getUniqueId()))
                    playersToPing.add(onlinePlayer);
            } else if (nickPattern.matcher(modifiableString.string()).find()) {
                modifiableString.replace(TextReplacementConfig.builder()
                        .once()
                        .match(nickPattern)
                        .replacement(mention.append(onlinePlayer.displayName()))
                        .build());
                if (!ChatUserManager.getChatUser(onlinePlayer.getUniqueId()).getIgnoredPlayers().contains(player.getUniqueId()))
                    playersToPing.add(onlinePlayer);
            }
        }
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