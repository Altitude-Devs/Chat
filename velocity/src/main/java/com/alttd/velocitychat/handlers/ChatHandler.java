package com.alttd.velocitychat.handlers;

import com.alttd.velocitychat.VelocityChat;
import com.alttd.chat.config.Config;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Party;
import com.alttd.chat.util.ALogger;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
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

        MiniMessage miniMessage = MiniMessage.get();

        List<Template> templates = new ArrayList<>(List.of(
                Template.of("sender", senderUser.getDisplayName()),
                Template.of("sendername", player.getUsername()),
                Template.of("receiver", targetUser.getDisplayName()),
                Template.of("receivername", player2.getUsername()),
                Template.of("message", GsonComponentSerializer.gson().deserialize(message)),
                Template.of("server", player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Altitude")));

        ServerConnection serverConnection;
        if(player.getCurrentServer().isPresent() && player2.getCurrentServer().isPresent()) {
            // redirect to the sender
            serverConnection = player.getCurrentServer().get();
            Component component = miniMessage.parse(Config.MESSAGESENDER, templates);
            ByteArrayDataOutput buf = ByteStreams.newDataOutput();
            buf.writeUTF("privatemessageout");
            buf.writeUTF(player.getUniqueId().toString());
            buf.writeUTF(player2.getUsername());
            buf.writeUTF(GsonComponentSerializer.gson().serialize(component));
            buf.writeUTF(player2.getUniqueId().toString());
            serverConnection.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), buf.toByteArray());

            //redirect to the receiver
            serverConnection = player2.getCurrentServer().get();
            component = miniMessage.parse(Config.MESSAGERECIEVER, templates);
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
//        MiniMessage miniMessage = MiniMessage.get();
//
//        List<Template> templates = new ArrayList<>(List.of(
//                Template.of("sender", senderUser.getDisplayName()),
//                Template.of("receiver", targetUser.getDisplayName()),
//                Template.of("message", message),
//                Template.of("server", player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Altitude")));
//
//        Component senderMessage = miniMessage.parse(Config.MESSAGESENDER, templates);
//        Component receiverMessage = miniMessage.parse(Config.MESSAGERECIEVER, templates);
//
//        player.sendMessage(senderMessage);
//        player2.sendMessage(receiverMessage);
    }

    public void globalAdminChat(String message) {
        Component component = GsonComponentSerializer.gson().deserialize(message);

        VelocityChat.getPlugin().getProxy().getAllPlayers().stream().filter(target -> target.hasPermission("command.proxy.globaladminchat")/*TODO permission*/).forEach(target -> {
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

        MiniMessage miniMessage = MiniMessage.get();

        List<Template> templates = new ArrayList<>(List.of(
                Template.of("message", message),
                Template.of("sender", senderName),
                Template.of("server", serverName)));

        Component component = miniMessage.parse(Config.GACFORMAT, templates);

        VelocityChat.getPlugin().getProxy().getAllPlayers().stream().filter(target -> target.hasPermission("command.proxy.globaladminchat")/*TODO permission*/).forEach(target -> {
            target.sendMessage(component);
        });
    }

    /**
     * constructs a mail object and notifies all involved players about it
     * / mail send playerA,playerB,playerC message
     */
    public void sendMail(CommandSource commandSource, String recipient, String message) {
        UUID uuid;
        if (commandSource instanceof Player) {
            uuid = ((Player) commandSource).getUniqueId();
        } else {
            uuid = Config.CONSOLEUUID;
        }
        Optional<Player> optionalPlayer = VelocityChat.getPlugin().getProxy().getPlayer(recipient);

        //Mail mail = new Mail()
        // todo construct the mail and notify the player if online?
    }

    public void readMail(CommandSource commandSource, String targetPlayer, boolean unread) {

    }

    public void readMail(CommandSource commandSource, boolean unread) {

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
}