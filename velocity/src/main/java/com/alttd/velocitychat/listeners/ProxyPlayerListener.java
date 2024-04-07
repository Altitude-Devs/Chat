package com.alttd.velocitychat.listeners;

import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Mail;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.VelocityChat;
import com.alttd.chat.config.Config;
import com.alttd.velocitychat.commands.vote_to_mute.ActiveVoteToMute;
import com.alttd.velocitychat.data.ServerWrapper;
import com.alttd.velocitychat.handlers.ServerHandler;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.Party;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.util.*;

public class ProxyPlayerListener {

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerLogin(LoginEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Party party = PartyManager.getParty(event.getPlayer().getUniqueId());
        if (party == null) return;
        ChatUser chatUser = ChatUserManager.getChatUser(uuid);
        if (chatUser == null)
            return;
        VelocityChat.getPlugin().getChatHandler().sendPartyMessage(party,
                Utility.parseMiniMessage(Config.PARTY_MEMBER_LOGGED_ON,
                        Placeholder.component("player", chatUser.getDisplayName())
                ),
                chatUser.getIgnoredPlayers());
        // TODO setup ChatUser on Proxy
        //VelocityChat.getPlugin().getChatHandler().addPlayer(new ChatPlayer(event.getPlayer().getUniqueId()));

        ServerHandler.addPlayerUUID(player.getUsername(), uuid);
    }

    @Subscribe(order = PostOrder.LAST)
    public void afterPlayerLogin(ServerPostConnectEvent event) {
        Player player = event.getPlayer();
        RegisteredServer previousServer = event.getPreviousServer();
        if (previousServer != null) {
            ActiveVoteToMute.removePotentialVoter(player, previousServer);
            Optional<ServerConnection> currentServer = player.getCurrentServer();
            if (currentServer.isEmpty())
                return;
            ActiveVoteToMute.addPotentialVoter(player, currentServer.get());
            return;
        }
        Optional<ServerConnection> currentServer = player.getCurrentServer();
        if (currentServer.isEmpty())
            return;
        ActiveVoteToMute.addPotentialVoter(player, currentServer.get());
        ChatUser chatUser = ChatUserManager.getChatUser(player.getUniqueId());
        List<Mail> unReadMail = chatUser.getUnReadMail();
        if (unReadMail.isEmpty())
            return;
        player.sendMessage(Utility.parseMiniMessage(Config.mailUnread,
                Placeholder.unparsed("amount", String.valueOf(unReadMail.size()))
        ));
    }

    @Subscribe
    public void quitEvent(DisconnectEvent event) {
        ActiveVoteToMute.removePotentialVoter(event.getPlayer(), null);
        UUID uuid = event.getPlayer().getUniqueId();
        Party party = PartyManager.getParty(event.getPlayer().getUniqueId());
        if (party == null) return;
        ChatUser chatUser = ChatUserManager.getChatUser(uuid);
        if (chatUser == null)
            return;
        VelocityChat.getPlugin().getChatHandler().sendPartyMessage(party,
                Utility.parseMiniMessage(Config.PARTY_MEMBER_LOGGED_OFF,
                        Placeholder.component("player", chatUser.getDisplayName())
                ),
                chatUser.getIgnoredPlayers());
        // TODO setup ChatUser on Proxy
        //VelocityChat.getPlugin().getChatHandler().removePlayer(event.getPlayer().getUniqueId());
    }


    private static final HashSet<UUID> silentJoin = new HashSet<>();

    public static void addSilentJoin(UUID uuid)
    {
        silentJoin.add(uuid);
    }

    // Server Join and Leave messages
    @Subscribe
    public void serverConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        ServerHandler serverHandler = VelocityChat.getPlugin().getServerHandler();
        if (event.getPreviousServer().isPresent()) {
            RegisteredServer previousServer = event.getPreviousServer().get();

            TagResolver placeholders = TagResolver.resolver(
                    Placeholder.parsed("player", player.getUsername()),
                    Placeholder.parsed("from_server", previousServer.getServerInfo().getName()),
                    Placeholder.parsed("to_server", event.getServer().getServerInfo().getName())
            );

            if (silentJoin.remove(uuid)) {
                Component message = Utility.parseMiniMessage(Config.SILENT_JOIN_JOINED_FROM,
                        placeholders);
                event.getServer().getPlayersConnected().stream()
                        .filter(player1 -> player1.hasPermission("command.chat.silent-join-notify"))
                        .forEach(player1 -> player1.sendMessage(message));
                return;
            }

            ServerWrapper wrapper = serverHandler.getWrapper(previousServer.getServerInfo().getName());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(uuid, Utility.parseMiniMessage(Config.SERVERSWTICHMESSAGETO, placeholders));
            }
            wrapper = serverHandler.getWrapper(event.getServer().getServerInfo().getName());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(uuid, Utility.parseMiniMessage(Config.SERVERSWTICHMESSAGEFROM, placeholders));
            }
        } else {
            if (silentJoin.remove(uuid)) {
                Component message = Utility.parseMiniMessage(Config.SILENT_JOIN_JOINED,
                        Placeholder.unparsed("player", player.getUsername()));
                event.getServer().getPlayersConnected().stream()
                        .filter(player1 -> player1.hasPermission("command.chat.silent-join-notify"))
                        .forEach(player1 -> player1.sendMessage(message));
                return;
            }

            ServerWrapper wrapper = serverHandler.getWrapper(event.getServer().getServerInfo().getName());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(uuid, Utility.parseMiniMessage(Config.SERVERJOINMESSAGE, Placeholder.unparsed("player", player.getUsername())));
            }
        }
    }

    @Subscribe
    public void serverDisconnected(DisconnectEvent event) {
        ServerHandler serverHandler = VelocityChat.getPlugin().getServerHandler();
        if (event.getLoginStatus().equals(DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) && event.getPlayer().getCurrentServer().isPresent()) {
            RegisteredServer registeredServer = event.getPlayer().getCurrentServer().get().getServer();

            ServerWrapper wrapper = serverHandler.getWrapper(registeredServer.getServerInfo().getName());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(event.getPlayer().getUniqueId(), Utility.parseMiniMessage(Config.SERVERLEAVEMESSAGE,
                        Placeholder.unparsed("player", event.getPlayer().getUsername()),
                        Placeholder.unparsed("from_server", registeredServer.getServerInfo().getName())
                ));
            }
        }
    }

}
