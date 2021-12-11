package com.alttd.velocitychat.listeners;

import com.alttd.velocitychat.VelocityChat;
import com.alttd.chat.config.Config;
import com.alttd.velocitychat.data.ServerWrapper;
import com.alttd.velocitychat.handlers.ServerHandler;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.Party;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProxyPlayerListener {

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerLogin(LoginEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Party party = PartyManager.getParty(event.getPlayer().getUniqueId());
        if (party == null) return;
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("partylogin");
        out.writeUTF(String.valueOf(party.getPartyId()));
        out.writeUTF(uuid.toString());
        VelocityChat.getPlugin().getProxy().getAllServers().forEach(registeredServer -> registeredServer.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), out.toByteArray()));
        // TODO setup ChatUser on Proxy
        //VelocityChat.getPlugin().getChatHandler().addPlayer(new ChatPlayer(event.getPlayer().getUniqueId()));
    }

    @Subscribe
    public void quitEvent(DisconnectEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        Party party = PartyManager.getParty(event.getPlayer().getUniqueId());
        if (party == null) return;
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("partylogout");
        out.writeUTF(String.valueOf(party.getPartyId()));
        out.writeUTF(uuid.toString());
        VelocityChat.getPlugin().getProxy().getAllServers().forEach(registeredServer -> registeredServer.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), out.toByteArray()));
        // TODO setup ChatUser on Proxy
        //VelocityChat.getPlugin().getChatHandler().removePlayer(event.getPlayer().getUniqueId());
    }

    // Server Join and Leave messages
    @Subscribe
    public void serverConnected(ServerConnectedEvent event) {
        ServerHandler serverHandler = VelocityChat.getPlugin().getServerHandler();
        MiniMessage miniMessage = MiniMessage.get();
        if (event.getPreviousServer().isPresent()) {
            RegisteredServer previousServer = event.getPreviousServer().get();

            Player player = event.getPlayer();

            List<Template> templates = new ArrayList<>(List.of(
                    Template.of("player", player.getUsername()),
                    Template.of("from_server", previousServer.getServerInfo().getName()),
                    Template.of("to_server", event.getServer().getServerInfo().getName())));
            ServerWrapper wrapper = serverHandler.getWrapper(previousServer.getServerInfo().getName());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(event.getPlayer().getUniqueId(), miniMessage.parse(Config.SERVERSWTICHMESSAGETO, templates));
            }
            wrapper = serverHandler.getWrapper(event.getServer().getServerInfo().getName());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(event.getPlayer().getUniqueId(), miniMessage.parse(Config.SERVERSWTICHMESSAGEFROM, templates));
            }
        } else {
            List<Template> templates = new ArrayList<>(List.of(
                    Template.of("player", event.getPlayer().getUsername())
            ));
            ServerWrapper wrapper = serverHandler.getWrapper(event.getServer().getServerInfo().getName());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(event.getPlayer().getUniqueId(), miniMessage.parse(Config.SERVERJOINMESSAGE, templates));
            }
        }
    }

    @Subscribe
    public void serverDisconnected(DisconnectEvent event) {
        ServerHandler serverHandler = VelocityChat.getPlugin().getServerHandler();
        MiniMessage miniMessage = MiniMessage.get();
        if (event.getLoginStatus().equals(DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) && event.getPlayer().getCurrentServer().isPresent()) {
            RegisteredServer registeredServer = event.getPlayer().getCurrentServer().get().getServer();

            List<Template> templates = new ArrayList<>(List.of(
                    Template.of("player", event.getPlayer().getUsername()),
                    Template.of("from_server", registeredServer.getServerInfo().getName())));

            ServerWrapper wrapper = serverHandler.getWrapper(registeredServer.getServerInfo().getName());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(event.getPlayer().getUniqueId(), miniMessage.parse(Config.SERVERLEAVEMESSAGE, templates));
            }
        }
    }
}
