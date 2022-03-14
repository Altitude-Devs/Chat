package com.alttd.velocitychat.handlers;

import com.alttd.chat.config.Config;
import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Mail;
import com.alttd.chat.objects.ModifiableString;
import com.alttd.chat.objects.Party;
import com.alttd.chat.util.ALogger;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.VelocityChat;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mysql.cj.MessageBuilder;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

        TagResolver Placeholders = TagResolver.resolver(
                Placeholder.component("sender", senderUser.getDisplayName()),
                Placeholder.unparsed("sendername", player.getUsername()),
                Placeholder.component("receiver", targetUser.getDisplayName()),
                Placeholder.unparsed("receivername", player2.getUsername()),
                Placeholder.component("message", GsonComponentSerializer.gson().deserialize(message)),
                Placeholder.unparsed("server", player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Altitude"));

        ServerConnection serverConnection;
        if(player.getCurrentServer().isPresent() && player2.getCurrentServer().isPresent()) {
            // redirect to the sender
            serverConnection = player.getCurrentServer().get();
            Component component = Utility.parseMiniMessage(Config.MESSAGESENDER, Placeholders);
            ByteArrayDataOutput buf = ByteStreams.newDataOutput();
            buf.writeUTF("privatemessageout");
            buf.writeUTF(player.getUniqueId().toString());
            buf.writeUTF(player2.getUsername());
            buf.writeUTF(GsonComponentSerializer.gson().serialize(component));
            buf.writeUTF(player2.getUniqueId().toString());
            serverConnection.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), buf.toByteArray());

            //redirect to the receiver
            serverConnection = player2.getCurrentServer().get();
            component = Utility.parseMiniMessage(Config.MESSAGERECIEVER, Placeholders);
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
        TagResolver Placeholders = TagResolver.resolver(
                Placeholder.unparsed("prefix", prefix),
                Placeholder.parsed("displayname", Utility.getDisplayName(player.getUniqueId(), player.getUsername())),
                Placeholder.unparsed("target", (target.isEmpty() ? " tried to say: " : " -> " + target + ": ")),
                Placeholder.unparsed("input", input)
        );
        Component blockedNotification = Utility.parseMiniMessage(Config.NOTIFICATIONFORMAT, Placeholders);

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
        ModifiableString modifiableString = new ModifiableString(message);
        if (!RegexManager.filterText(player.getUsername(), uuid, modifiableString, "party")) {
            sendBlockedNotification("Party Language", player, message, "", serverConnection);
            return; // the message was blocked
        }

        String updatedMessage = modifiableString.string();
        if(!player.hasPermission("chat.format")) {
            updatedMessage = Utility.stripTokens(updatedMessage);
        }

        if(updatedMessage.contains("[i]")) updatedMessage = updatedMessage.replace("[i]", "<[i]>");

        updatedMessage = Utility.formatText(updatedMessage);

        TagResolver Placeholders = TagResolver.resolver(
                Placeholder.component("sender", senderName),
                Placeholder.component("sendername", senderName),
                Placeholder.unparsed("partyname", party.getPartyName()),
                Placeholder.unparsed("message", updatedMessage),
                Placeholder.unparsed("server", serverConnection.getServer().getServerInfo().getName()),
                Placeholder.component("[i]", item)
        );

        Component partyMessage = Utility.parseMiniMessage(Config.PARTY_FORMAT, Placeholders);
        sendPartyMessage(party, partyMessage, user.getIgnoredBy());

        Component spyMessage = Utility.parseMiniMessage(Config.PARTY_SPY, Placeholders);
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

        TagResolver Placeholders = TagResolver.resolver(
                Placeholder.unparsed("message", message),
                Placeholder.component("sender", senderName),
                Placeholder.unparsed("server", serverName));

        Component component = Utility.parseMiniMessage(Config.GACFORMAT, Placeholders);

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
        Mail mail = new Mail(uuid, targetUUID, message);
        ChatUser chatUser = ChatUserManager.getChatUser(targetUUID);
        chatUser.addMail(mail);
        // TODO load from config
        String finalSenderName = senderName;
        optionalPlayer.ifPresent(player -> player.sendMessage(Utility.parseMiniMessage("<yellow>New mail from " + finalSenderName)));
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
            TagResolver Placeholders = TagResolver.resolver(
                    Placeholder.component("staffprefix", chatUser.getStaffPrefix()),
                    Placeholder.component("sender", chatUser.getDisplayName()),
                    Placeholder.unparsed("message", mail.getMessage()),
                    Placeholder.unparsed("date", new Date(mail.getSendTime()).toString())
            );
            Component mailMessage = Utility.parseMiniMessage(Config.mailBody, Placeholders);
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