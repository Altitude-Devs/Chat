package com.alttd.velocitychat.handlers;

import com.alttd.chat.config.Config;
import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Mail;
import com.alttd.chat.objects.Party;
import com.alttd.chat.util.ALogger;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.VelocityChat;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChatHandler {

    public void privateMessage(String sender, String target, String message) {
        UUID uuid = UUID.fromString(sender);
        ChatUser senderUser = ChatUserManager.getChatUser(uuid);
        Optional<Player> optionalPlayer = VelocityChat.getPlugin().getProxy().getPlayer(uuid);
        if(optionalPlayer.isEmpty()) return;
        Player player = optionalPlayer.get();

        Optional<Player> optionalPlayer2 = VelocityChat.getPlugin().getProxy().getPlayer(target);
        if(optionalPlayer2.isEmpty()) return;
        Player player2 = optionalPlayer2.get();
        ChatUser targetUser = ChatUserManager.getChatUser(player2.getUniqueId());

        TagResolver placeholders = TagResolver.resolver(
                Placeholder.component("sender", senderUser.getDisplayName()),
                Placeholder.unparsed("sendername", player.getUsername()),
                Placeholder.component("receiver", targetUser.getDisplayName()),
                Placeholder.unparsed("receivername", player2.getUsername()),
                Placeholder.component("message", GsonComponentSerializer.gson().deserialize(message)),
                Placeholder.unparsed("server", player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Altitude")
        );

        ServerConnection serverConnection;
        if(player.getCurrentServer().isPresent() && player2.getCurrentServer().isPresent()) {
            // redirect to the sender
            serverConnection = player.getCurrentServer().get();
            Component component = Utility.parseMiniMessage(Config.MESSAGESENDER, placeholders);
            ByteArrayDataOutput buf = ByteStreams.newDataOutput();
            buf.writeUTF("privatemessageout");
            buf.writeUTF(player.getUniqueId().toString());
            buf.writeUTF(player2.getUsername());
            buf.writeUTF(GsonComponentSerializer.gson().serialize(component));
            buf.writeUTF(player2.getUniqueId().toString());
            serverConnection.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), buf.toByteArray());

            //redirect to the receiver
            serverConnection = player2.getCurrentServer().get();
            component = Utility.parseMiniMessage(Config.MESSAGERECIEVER, placeholders);
            buf = ByteStreams.newDataOutput();
            buf.writeUTF("privatemessagein");
            buf.writeUTF(player2.getUniqueId().toString());
            buf.writeUTF(player.getUsername());
            buf.writeUTF(GsonComponentSerializer.gson().serialize(component));
            buf.writeUTF(player.getUniqueId().toString());
            serverConnection.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), buf.toByteArray());
        }

    }

    public static void sendBlockedNotification(String prefix, Player player, String input, String target, ServerConnection serverConnection) {
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.parsed("prefix", prefix),
                Placeholder.parsed("displayname", Utility.getDisplayName(player.getUniqueId(), player.getUsername())),
                Placeholder.parsed("target", (target.isEmpty() ? " tried to say: " : " -> " + target + ": ")),
                Placeholder.parsed("input", input)
        );
        Component blockedNotification = Utility.parseMiniMessage(Config.NOTIFICATIONFORMAT, placeholders);

        serverConnection.getServer().getPlayersConnected().forEach(pl ->{
            if (pl.hasPermission("chat.alert-blocked")) {
                pl.sendMessage(blockedNotification);
            }
        });
        player.sendMessage(Utility.parseMiniMessage("<red>The language you used in your message is not allowed, " +
                "this constitutes as your only warning. Any further attempts at bypassing the filter will result in staff intervention.</red>"));
    }

    public void sendPartyMessage(Party party, Component message, @Nullable List<UUID> ignoredPlayers)
    {
        VelocityChat.getPlugin().getProxy().getAllPlayers().stream()
                .filter(pl -> {
                    UUID uuid = pl.getUniqueId();
                    if (ignoredPlayers != null && ignoredPlayers.contains(uuid))
                        return false;
                    return party.getPartyUsers().stream().anyMatch(pu -> pu.getUuid().equals(uuid));
                }).forEach(pl -> {
                    pl.sendMessage(message);
                });
    }

    public void sendPartyMessage(UUID uuid, String message, Component item, ServerConnection serverConnection) {
        Optional<Player> optionalPlayer = VelocityChat.getPlugin().getProxy().getPlayer(uuid);
        if (optionalPlayer.isEmpty()) return;
        Player player = optionalPlayer.get();
        ChatUser user = ChatUserManager.getChatUser(uuid);
        Party party = PartyManager.getParty(user.getPartyId());
        if (party == null) {
            player.sendMessage(Utility.parseMiniMessage(Config.NOT_IN_A_PARTY));
            return;
        }
        Component senderName = user.getDisplayName();

        String updatedMessage = RegexManager.replaceText(player.getUsername(), uuid, message);
        if(updatedMessage == null) {
            sendBlockedNotification("Party Language", player, message, "", serverConnection);
            return; // the message was blocked
        }

        if(!player.hasPermission("chat.format")) {
            updatedMessage = Utility.stripTokens(updatedMessage);
        }

        if(updatedMessage.contains("[i]")) updatedMessage = updatedMessage.replace("[i]", "<[i]>");

        updatedMessage = Utility.formatText(updatedMessage);

        TagResolver placeholders = TagResolver.resolver(
                Placeholder.component("sender", senderName),
                Placeholder.unparsed("username", player.getUsername()),
                Placeholder.unparsed("party", party.getPartyName()),
                Placeholder.parsed("message", updatedMessage),
                Placeholder.unparsed("server", serverConnection.getServer().getServerInfo().getName()),
                Placeholder.component("[i]", item)
        );

        Component partyMessage = Utility.parseMiniMessage(Config.PARTY_FORMAT, placeholders);
        sendPartyMessage(party, partyMessage, user.getIgnoredBy());

        Component spyMessage = Utility.parseMiniMessage(Config.PARTY_SPY, placeholders);
        for(Player pl : serverConnection.getServer().getPlayersConnected()) {
            if(pl.hasPermission(Config.SPYPERMISSION) && !party.getPartyUsersUuid().contains(pl.getUniqueId())) {
                pl.sendMessage(spyMessage);
            }
        }
    }

    public void globalAdminChat(String message) {
        Component component = GsonComponentSerializer.gson().deserialize(message);

        VelocityChat.getPlugin().getProxy().getAllPlayers().stream().filter(target -> target.hasPermission("command.chat.globaladminchat")/*TODO permission*/).forEach(target -> {
            target.sendMessage(component);
        });
    }

    public void globalAdminChat(CommandSource commandSource, String message) {
        Component senderName = Component.text(Config.CONSOLENAME);
        String serverName = "Altitude";
        if (commandSource instanceof Player) {
            Player sender = (Player) commandSource;
            ChatUser user = ChatUserManager.getChatUser(sender.getUniqueId());
            if(user == null) return;
            senderName = user.getDisplayName();
            serverName = sender.getCurrentServer().isPresent() ? sender.getCurrentServer().get().getServerInfo().getName() : "Altitude";
        }

        TagResolver placeholders = TagResolver.resolver(
                Placeholder.parsed("message", Utility.formatText(message)),
                Placeholder.component("sender", senderName),
                Placeholder.parsed("server", serverName)
        );

        Component component = Utility.parseMiniMessage(Config.GACFORMAT, placeholders);

        VelocityChat.getPlugin().getProxy().getAllPlayers().stream().filter(target -> target.hasPermission("command.chat.globaladminchat")/*TODO permission*/).forEach(target -> {
            target.sendMessage(component);
        });
    }

    public void sendMail(CommandSource commandSource, String recipient, String message) {
        UUID uuid = Config.CONSOLEUUID;;
        String senderName = Config.CONSOLENAME;
        UUID targetUUID;
        if (commandSource instanceof Player player) {
            uuid = player.getUniqueId();
            senderName = player.getUsername();
        }

        Optional<Player> optionalPlayer = VelocityChat.getPlugin().getProxy().getPlayer(recipient);
        if (optionalPlayer.isEmpty()) {
            targetUUID = ServerHandler.getPlayerUUID(recipient);
            if (targetUUID == null) {
                commandSource.sendMessage(Utility.parseMiniMessage("<red>A player with this name hasn't logged in recently.")); // TOOD load from config
                return;
            }
        } else {
            targetUUID = optionalPlayer.get().getUniqueId();
        }

        if (!commandSource.hasPermission("chat.format"))
            message = Utility.stripTokens(message);
        else
            message = Utility.parseColors(message);

        Mail mail = new Mail(targetUUID, uuid, message);
        ChatUser chatUser = ChatUserManager.getChatUser(targetUUID);
        chatUser.addMail(mail);
        // TODO load from config
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.parsed("sender", senderName)
        );
        optionalPlayer.ifPresent(player -> player.sendMessage(Utility.parseMiniMessage(Config.mailReceived, placeholders)));
        commandSource.sendMessage(Utility.parseMiniMessage(Config.mailSent,
                Placeholder.component("player_name", chatUser.getDisplayName()),
                Placeholder.parsed("message", message)
        ));
    }

    public void readMail(CommandSource commandSource, String targetPlayer) {
        UUID uuid = ServerHandler.getPlayerUUID(targetPlayer);
        if (uuid == null) {
            commandSource.sendMessage(Utility.parseMiniMessage(Config.mailNoUser));
            return;
        }
        ChatUser chatUser = ChatUserManager.getChatUser(uuid);
        commandSource.sendMessage(parseMails(chatUser.getMails(), false));
    }

    public void readMail(CommandSource commandSource, boolean unread) {
        if (commandSource instanceof Player player) {
            ChatUser chatUser = ChatUserManager.getChatUser(player.getUniqueId());
            commandSource.sendMessage(parseMails(unread ? chatUser.getUnReadMail() : chatUser.getMails(), unread));
        }
    }

    private Component parseMails(List<Mail> mails, boolean mark) {
        Component component = Utility.parseMiniMessage(Config.mailHeader);
        for (Mail mail : mails) {
            if (mail.isUnRead() && mark) {
                mail.setReadTime(System.currentTimeMillis());
                Queries.markMailRead(mail);
            }
            ChatUser chatUser = ChatUserManager.getChatUser(mail.getSender());
            Date sentTime = new Date(mail.getSendTime());
            TagResolver placeholders = TagResolver.resolver(
                    Placeholder.component("staffprefix", chatUser.getStaffPrefix()),
                    Placeholder.component("sender", chatUser.getDisplayName()),
                    Placeholder.parsed("message", mail.getMessage()),
                    Placeholder.parsed("date", sentTime.toString()),
                    Placeholder.parsed("time_ago", String.valueOf(TimeUnit.MILLISECONDS.toDays(new Date().getTime() - sentTime.getTime())))
            );
            Component mailMessage = Utility.parseMiniMessage(Config.mailBody, placeholders);
            component = component.append(Component.newline()).append(mailMessage);
        }
        component = component.append(Component.newline()).append(Utility.parseMiniMessage(Config.mailFooter));
        return component;
    }

    public void partyChat(String partyId, UUID uuid, Component message) {
        Party party = PartyManager.getParty(Integer.parseInt(partyId));
        if (party == null) {
            ALogger.warn("Received a non existent party");
            return;
        }
        List<UUID> ignoredPlayers = ChatUserManager.getChatUser(uuid).getIgnoredPlayers();
        List<UUID> partyUsersUuid = party.getPartyUsersUuid();
        VelocityChat.getPlugin().getProxy().getAllPlayers().stream()
                .filter(p -> partyUsersUuid.contains(p.getUniqueId()))
                .filter(p -> !ignoredPlayers.contains(p.getUniqueId()))
                .forEach(p -> p.sendMessage(message));
    }

    public void mutePlayer(String uuid, boolean muted) {
        ByteArrayDataOutput buf = ByteStreams.newDataOutput();
        buf.writeUTF("chatpunishments");
        buf.writeUTF(uuid);
        buf.writeBoolean(muted);
    }
}