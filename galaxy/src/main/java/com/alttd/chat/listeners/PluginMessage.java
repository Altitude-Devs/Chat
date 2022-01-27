package com.alttd.chat.listeners;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.Party;
import com.alttd.chat.objects.PartyUser;
import com.alttd.chat.objects.channels.Channel;
import com.alttd.chat.objects.channels.CustomChannel;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.util.ALogger;
import com.alttd.chat.util.Utility;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PluginMessage implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (!channel.equals(Config.MESSAGECHANNEL)) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String subChannel = in.readUTF();
        switch (subChannel) {
            case "privatemessagein": {
                UUID uuid = UUID.fromString(in.readUTF());
                String target = in.readUTF();
                Player p = Bukkit.getPlayer(uuid);
                String message = in.readUTF();
                UUID targetuuid = UUID.fromString(in.readUTF());
                if (p != null) {
                    ChatUser chatUser = ChatUserManager.getChatUser(uuid);
                    if (!chatUser.getIgnoredPlayers().contains(targetuuid)) {
                        p.sendMessage(GsonComponentSerializer.gson().deserialize(message));
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1); // todo load this from config
                        ChatUser user = ChatUserManager.getChatUser(uuid);
                        user.setReplyTarget(target);
                    }
                }
                break;
            }
            case "privatemessageout": {
                UUID uuid = UUID.fromString(in.readUTF());
                String target = in.readUTF();
                Player p = Bukkit.getPlayer(uuid);
                String message = in.readUTF();
                UUID targetuuid = UUID.fromString(in.readUTF());
                if (p != null) {
                    ChatUser chatUser = ChatUserManager.getChatUser(uuid);
                    if (!chatUser.getIgnoredPlayers().contains(targetuuid)) {
                        p.sendMessage(GsonComponentSerializer.gson().deserialize(message));
                        ChatUser user = ChatUserManager.getChatUser(uuid);
                        user.setReplyTarget(target);
                    }
                }
                break;
            }
            case "globalchat": {
                if (!ChatPlugin.getInstance().serverGlobalChatEnabled() || ChatPlugin.getInstance().serverMuted()) break;

                UUID uuid = UUID.fromString(in.readUTF());
                String message = in.readUTF();

                Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission(Config.GCPERMISSION)).forEach(p -> {
                    ChatUser chatUser = ChatUserManager.getChatUser(p.getUniqueId());
                    if (!chatUser.getIgnoredPlayers().contains(uuid)) {
                        p.sendMessage(GsonComponentSerializer.gson().deserialize(message));
                    }
                });
                break;
            }
            case "ignore": {
                ChatUser chatUser = ChatUserManager.getChatUser(UUID.fromString(in.readUTF()));
                UUID targetUUID = UUID.fromString(in.readUTF());

                if(!chatUser.getIgnoredPlayers().contains(targetUUID)) {
                    chatUser.addIgnoredPlayers(targetUUID);
                }
                break;
            }
            case "unignore": {
                ChatUser chatUser = ChatUserManager.getChatUser(UUID.fromString(in.readUTF()));
                chatUser.removeIgnoredPlayers(UUID.fromString(in.readUTF()));
                break;
            }
            case "chatchannel": {
                if (ChatPlugin.getInstance().serverMuted()) break;

                chatChannel(in);
                break;
            }
            case "tmppartyupdate" : {
                int id = Integer.parseInt(in.readUTF());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Queries.loadPartyUsers(id);
                    }
                }.runTaskAsynchronously(ChatPlugin.getInstance());
                break;
            }
            case "partylogin": {
                int id = Integer.parseInt(in.readUTF());
                Party party = PartyManager.getParty(id);
                if (party == null) {
                    ALogger.warn("Received invalid party id.");
                    return;
                }
                UUID uuid = UUID.fromString(in.readUTF());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        PartyUser user = party.getPartyUser(uuid);
                        if(user != null) {
                            Component component = Utility.parseMiniMessage("<dark_aqua>* " + user.getPlayerName() + " logged in to Altitude.");

                            Bukkit.getOnlinePlayers().stream()
                                    .filter(p -> party.getPartyUsersUuid().contains(p.getUniqueId()))
                                    .filter(p -> !ChatUserManager.getChatUser(p.getUniqueId()).getIgnoredPlayers().contains(uuid))
                                    .forEach(p -> p.sendMessage(component));
                        }
                    }
                }.runTaskAsynchronously(ChatPlugin.getInstance());
                break;
            }
            case "partylogout": {
                int id = Integer.parseInt(in.readUTF());
                Party party = PartyManager.getParty(id);
                if (party == null) {
                    ALogger.warn("Received invalid party id.");
                    return;
                }
                UUID uuid = UUID.fromString(in.readUTF());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        PartyUser user = party.getPartyUser(uuid);
                        if(user != null) {
                            Component component = Utility.parseMiniMessage("<dark_aqua>* " + user.getPlayerName() + " logged out of Altitude.");

                            Bukkit.getOnlinePlayers().stream()
                                    .filter(p -> party.getPartyUsersUuid().contains(p.getUniqueId()))
                                    .filter(p -> !ChatUserManager.getChatUser(p.getUniqueId()).getIgnoredPlayers().contains(uuid))
                                    .forEach(p -> p.sendMessage(component));
                        }
                    }
                }.runTaskAsynchronously(ChatPlugin.getInstance());
                break;
            }
            case "reloadconfig":
                ChatPlugin.getInstance().ReloadConfig();
                break;
            case "chatpunishments":
                UUID uuid = UUID.fromString(in.readUTF());
                boolean mute = in.readBoolean();
                ChatUser user = ChatUserManager.getChatUser(uuid);
                if (user == null) return;
                user.setMuted(mute);
                break;
            default:
                break;
        }
    }

    private void chatChannel(ByteArrayDataInput in) {
        CustomChannel chatChannel = null;
        UUID uuid = null;
        Component component = null;
        try {
            chatChannel = (CustomChannel) Channel.getChatChannel(in.readUTF());
            uuid = UUID.fromString(in.readUTF());
            component = GsonComponentSerializer.gson().deserialize(in.readUTF());
        } catch (Exception e) { //Idk the exception for reading too far into in.readUTF()
            e.printStackTrace();
        }

        if (chatChannel == null) {
            ALogger.warn("Received ChatChannel message for non existent channel.");
            return;
        }
        if (!chatChannel.getServers().contains(Bukkit.getServerName())) {
            ALogger.warn("Received ChatChannel message for the wrong server.");
            return;
        }
        if (component == null) {
            ALogger.warn("Didn't receive a valid message for ChatChannel " + chatChannel.getChannelName() + ".");
        }

        final CustomChannel finalChatChannel = chatChannel;
        final Component finalComponent = component;
        final UUID finalUuid = uuid;

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getOnlinePlayers().stream()
                        .filter(p -> p.hasPermission(finalChatChannel.getPermission()))
                        .filter(p -> !ChatUserManager.getChatUser(p.getUniqueId()).getIgnoredPlayers().contains(finalUuid))
                        .forEach(p -> p.sendMessage(finalComponent));
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());
    }

}