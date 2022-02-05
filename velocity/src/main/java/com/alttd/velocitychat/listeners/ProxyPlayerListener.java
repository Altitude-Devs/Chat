package com.alttd.velocitychat.listeners;

import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Mail;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.VelocityChat;
import com.alttd.chat.config.Config;
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
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.minimessage.Template;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
                Utility.parseMiniMessage(Config.PARTY_MEMBER_LOGGED_ON, List.of(
                        Template.template("player", chatUser.getDisplayName())
                )),
                chatUser.getIgnoredPlayers());
        // TODO setup ChatUser on Proxy
        //VelocityChat.getPlugin().getChatHandler().addPlayer(new ChatPlayer(event.getPlayer().getUniqueId()));

        ServerHandler.addPlayerUUID(player.getUsername(), uuid);
    }

    @Subscribe(order = PostOrder.LAST)
    public void afterPlayerLogin(ServerPostConnectEvent event) {
        if (event.getPreviousServer() != null)
            return;
        Player player = event.getPlayer();
        ChatUser chatUser = ChatUserManager.getChatUser(player.getUniqueId());
        List<Mail> unReadMail = chatUser.getUnReadMail();
        if (unReadMail.isEmpty())
            return;
        player.sendMessage(Utility.parseMiniMessage(Config.mailUnread, List.of(
                Template.template("amount", String.valueOf(unReadMail.size()))
        )));
    }

    @Subscribe
    public void quitEvent(DisconnectEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Party party = PartyManager.getParty(event.getPlayer().getUniqueId());
        if (party == null) return;
        ChatUser chatUser = ChatUserManager.getChatUser(uuid);
        if (chatUser == null)
            return;
        VelocityChat.getPlugin().getChatHandler().sendPartyMessage(party,
                Utility.parseMiniMessage(Config.PARTY_MEMBER_LOGGED_OFF, List.of(
                        Template.template("player", chatUser.getDisplayName())
                )),
                chatUser.getIgnoredPlayers());
        // TODO setup ChatUser on Proxy
        //VelocityChat.getPlugin().getChatHandler().removePlayer(event.getPlayer().getUniqueId());
    }

    // Server Join and Leave messages
    @Subscribe
    public void serverConnected(ServerConnectedEvent event) {
        ServerHandler serverHandler = VelocityChat.getPlugin().getServerHandler();
        if (event.getPreviousServer().isPresent()) {
            RegisteredServer previousServer = event.getPreviousServer().get();

            Player player = event.getPlayer();

            List<Template> templates = new ArrayList<>(List.of(
                    Template.template("player", player.getUsername()),
                    Template.template("from_server", previousServer.getServerInfo().getName()),
                    Template.template("to_server", event.getServer().getServerInfo().getName())));
            ServerWrapper wrapper = serverHandler.getWrapper(previousServer.getServerInfo().getName());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(event.getPlayer().getUniqueId(), Utility.parseMiniMessage(Config.SERVERSWTICHMESSAGETO, templates));
            }
            wrapper = serverHandler.getWrapper(event.getServer().getServerInfo().getName());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(event.getPlayer().getUniqueId(), Utility.parseMiniMessage(Config.SERVERSWTICHMESSAGEFROM, templates));
            }
        } else {
            List<Template> templates = new ArrayList<>(List.of(
                    Template.template("player", event.getPlayer().getUsername())
            ));
            ServerWrapper wrapper = serverHandler.getWrapper(event.getServer().getServerInfo().getName());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(event.getPlayer().getUniqueId(), Utility.parseMiniMessage(Config.SERVERJOINMESSAGE, templates));
            }
        }
    }

    @Subscribe
    public void serverDisconnected(DisconnectEvent event) {
        ServerHandler serverHandler = VelocityChat.getPlugin().getServerHandler();
        if (event.getLoginStatus().equals(DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) && event.getPlayer().getCurrentServer().isPresent()) {
            RegisteredServer registeredServer = event.getPlayer().getCurrentServer().get().getServer();

            List<Template> templates = new ArrayList<>(List.of(
                    Template.template("player", event.getPlayer().getUsername()),
                    Template.template("from_server", registeredServer.getServerInfo().getName())));

            ServerWrapper wrapper = serverHandler.getWrapper(registeredServer.getServerInfo().getName());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(event.getPlayer().getUniqueId(), Utility.parseMiniMessage(Config.SERVERLEAVEMESSAGE, templates));
            }
        }
    }

}
