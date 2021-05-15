package com.alttd.chat.listeners;

import com.alttd.chat.VelocityChat;
import com.alttd.chat.config.Config;
import com.alttd.chat.data.ServerWrapper;
import com.alttd.chat.handlers.ServerHandler;
import com.alttd.chat.objects.ChatPlayer;
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

public class PlayerListener {

    private final ServerHandler serverHandler;
    private final MiniMessage miniMessage;

    public PlayerListener() {
        serverHandler = VelocityChat.getPlugin().getServerHandler();
        miniMessage = MiniMessage.get();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerLogin(LoginEvent event) {
        VelocityChat.getPlugin().getChatHandler().addPlayer(new ChatPlayer(event.getPlayer().getUniqueId()));
    }

    @Subscribe
    public void quitEvent(DisconnectEvent event) {
        VelocityChat.getPlugin().getChatHandler().removePlayer(event.getPlayer().getUniqueId());
    }

    // Server Join and Leave messages
    @Subscribe
    public void serverConnected(ServerConnectedEvent event) {
        if (event.getPreviousServer().isPresent()) {
            RegisteredServer previousServer = event.getPreviousServer().get();

            Player player = event.getPlayer();

            List<Template> templates = new ArrayList<>(List.of(
                    Template.of("player", player.getUsername()),
                    Template.of("from_server", previousServer.getServerInfo().getName()),
                    Template.of("to_server", event.getServer().getServerInfo().getName())));
            // todo Code clean up @Destro
            ServerWrapper wrapper = serverHandler.getWrapper(previousServer.getServerInfo().toString());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(miniMessage.parse(Config.SERVERSWTICHMESSAGETO, templates));
            }
            wrapper = serverHandler.getWrapper(event.getServer().getServerInfo().toString());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(miniMessage.parse(Config.SERVERSWTICHMESSAGEFROM, templates));
            }
        } else {
            List<Template> templates = new ArrayList<>(List.of(
                    Template.of("player", event.getPlayer().getUsername())
            ));
            ServerWrapper wrapper = serverHandler.getWrapper(event.getServer().getServerInfo().toString());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(miniMessage.parse(Config.SERVERJOINMESSAGE, templates));
            }
        }
    }

    @Subscribe
    public void serverDisconnected(DisconnectEvent event) {
        if (event.getLoginStatus().equals(DisconnectEvent.LoginStatus.SUCCESSFUL_LOGIN) && event.getPlayer().getCurrentServer().isPresent()) {
            RegisteredServer registeredServer = event.getPlayer().getCurrentServer().get().getServer();

            List<Template> templates = new ArrayList<>(List.of(
                    Template.of("player", event.getPlayer().getUsername()),
                    Template.of("from_server", registeredServer.getServerInfo().getName())));

            ServerWrapper wrapper = serverHandler.getWrapper(registeredServer.getServerInfo().toString());
            if(wrapper != null) {
                wrapper.sendJoinLeaveMessage(miniMessage.parse(Config.SERVERJOINMESSAGE, templates));
            }
        }
    }
}
