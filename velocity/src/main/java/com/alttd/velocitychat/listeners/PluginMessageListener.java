package com.alttd.velocitychat.listeners;

import com.alttd.velocitychat.VelocityChat;
import com.alttd.chat.database.Queries;
import com.alttd.chat.objects.channels.CustomChannel;
import com.alttd.chat.util.ALogger;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.UUID;

public class PluginMessageListener {

    //todo add an extra listener for nicknames?
    private final ChannelIdentifier identifier;

    public PluginMessageListener(ChannelIdentifier identifier){
        this.identifier = identifier;
    }

    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event){
        if (!event.getIdentifier().equals(identifier)) return;
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if(event.getSource() instanceof Player) {
            ALogger.warn("Received plugin message from a player");
            return;
        }
        if (!(event.getSource() instanceof ServerConnection serverConnection)) return;
        // Read the data written to the message
        ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
        String channel = in.readUTF();
        VelocityChat.getPlugin().getLogger().info("server " + event.getSource());
        switch (channel) {
            case "globalchat":
                VelocityChat.getPlugin().getServerHandler().sendGlobalChat(in.readUTF(), in.readUTF());
                break;
            case "globaladminchat":
                VelocityChat.getPlugin().getChatHandler().globalAdminChat(in.readUTF());
                break;
            case "privatemessage":
                VelocityChat.getPlugin().getChatHandler().privateMessage(in.readUTF(), in.readUTF(), in.readUTF());
                break;
            case "chatchannel": {
                String channelName = in.readUTF();
                CustomChannel chatChannel = (CustomChannel) CustomChannel.getChatChannel(channelName);

                if (chatChannel == null) {
                    ALogger.warn("Received non existent channel" + channelName +".");
                    break;
                }

                ProxyServer proxy = VelocityChat.getPlugin().getProxy();
                chatChannel.getServers().forEach(server -> proxy.getServer(server).ifPresent(registeredServer ->
                        registeredServer.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), event.getData())));
                break;
            }
            case "party": {
                VelocityChat.getPlugin().getChatHandler().sendPartyMessage(
                        UUID.fromString(in.readUTF()),
                        in.readUTF(),
                        GsonComponentSerializer.gson().deserialize(in.readUTF()),
                        serverConnection);
                break;
            }
            default:
                VelocityChat.getPlugin().getLogger().info("server " + event.getSource());
                ProxyServer proxy = VelocityChat.getPlugin().getProxy();

                for (RegisteredServer registeredServer : proxy.getAllServers()) {
                    registeredServer.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), event.getData());
                }
                break;
        }
    }

}
