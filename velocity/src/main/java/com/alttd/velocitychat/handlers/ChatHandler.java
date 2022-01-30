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
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

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

        List<Template> templates = new ArrayList<>(List.of(
                Template.template("sender", senderUser.getDisplayName()),
                Template.template("sendername", player.getUsername()),
                Template.template("receiver", targetUser.getDisplayName()),
                Template.template("receivername", player2.getUsername()),
                Template.template("message", GsonComponentSerializer.gson().deserialize(message)),
                Template.template("server", player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Altitude")));

        ServerConnection serverConnection;
        if(player.getCurrentServer().isPresent() && player2.getCurrentServer().isPresent()) {
            // redirect to the sender
            serverConnection = player.getCurrentServer().get();
            Component component = Utility.parseMiniMessage(Config.MESSAGESENDER, templates);
            ByteArrayDataOutput buf = ByteStreams.newDataOutput();
            buf.writeUTF("privatemessageout");
            buf.writeUTF(player.getUniqueId().toString());
            buf.writeUTF(player2.getUsername());
            buf.writeUTF(GsonComponentSerializer.gson().serialize(component));
            buf.writeUTF(player2.getUniqueId().toString());
            serverConnection.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), buf.toByteArray());

            //redirect to the receiver
            serverConnection = player2.getCurrentServer().get();
            component = Utility.parseMiniMessage(Config.MESSAGERECIEVER, templates);
            buf = ByteStreams.newDataOutput();
            buf.writeUTF("privatemessagein");
            buf.writeUTF(player2.getUniqueId().toString());
            buf.writeUTF(player.getUsername());
            buf.writeUTF(GsonComponentSerializer.gson().serialize(component));
            buf.writeUTF(player.getUniqueId().toString());
            serverConnection.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), buf.toByteArray());
        }

//        ChatUser targetUser = ChatUserManager.getChatUser(player2.getUniqueId());
//
//        List<Template> templates = new ArrayList<>(List.of(
//                Template.template("sender", senderUser.getDisplayName()),
//                Template.template("receiver", targetUser.getDisplayName()),
//                Template.template("message", message),
//                Template.template("server", player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Altitude")));
//
//        Component senderMessage = Utility.parseMiniMessage(Config.MESSAGESENDER, templates);
//        Component receiverMessage = Utility.parseMiniMessage(Config.MESSAGERECIEVER, templates);
//
//        player.sendMessage(senderMessage);
//        player2.sendMessage(receiverMessage);
    }

    public void sendPartyMessage(Party party, Component message)
    {
        VelocityChat.getPlugin().getProxy().getAllPlayers().stream()
                .filter(pl -> {
                    UUID uuid = pl.getUniqueId();
                    return party.getPartyUsers().stream().anyMatch(pu -> pu.getUuid().equals(uuid));
                }).forEach(pl -> {
                    pl.sendMessage(message);
                });
    }

    public void sendPartyMessage(Party party, Player player, String message, ServerConnection serverConnection) {
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        Component senderName = user.getDisplayName();

        String updatedMessage = RegexManager.replaceText(player.getUsername(), player.getUniqueId(), message);
        if(updatedMessage == null) {
//            GalaxyUtility.sendBlockedNotification("Party Language", player, message, "");
            return; // the message was blocked
        }

        if(!player.hasPermission("chat.format")) {
            updatedMessage = Utility.stripTokens(updatedMessage);
        }

        if(updatedMessage.contains("[i]")) updatedMessage = updatedMessage.replace("[i]", "<[i]>");

        updatedMessage = Utility.formatText(updatedMessage);

        List<Template> templates = new ArrayList<>(List.of(
                Template.template("sender", senderName),
                Template.template("sendername", senderName),
                Template.template("partyname", party.getPartyName()),
                Template.template("message", updatedMessage),
                Template.template("server", serverConnection.getServer().getServerInfo().getName()),
                Template.template("[i]", "item")
        ));

        Component partyMessage = Utility.parseMiniMessage(Config.PARTY_FORMAT, templates);
        sendPartyMessage(party, partyMessage);

        Component spyMessage = Utility.parseMiniMessage(Config.PARTY_SPY, templates);
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

        List<Template> templates = new ArrayList<>(List.of(
                Template.template("message", message),
                Template.template("sender", senderName),
                Template.template("server", serverName)));

        Component component = Utility.parseMiniMessage(Config.GACFORMAT, templates);

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
            List<Template> templates = new ArrayList<>(List.of(
                    Template.template("staffprefix", chatUser.getStaffPrefix()),
                    Template.template("sender", chatUser.getDisplayName()),
                    Template.template("message", mail.getMessage()),
                    Template.template("date", new Date(mail.getSendTime()).toString())
            ));
            Component mailMessage = Utility.parseMiniMessage(Config.mailBody, templates);
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