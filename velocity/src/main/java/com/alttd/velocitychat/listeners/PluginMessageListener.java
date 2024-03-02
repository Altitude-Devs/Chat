package com.alttd.velocitychat.listeners;

import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.channels.CustomChannel;
import com.alttd.chat.util.ALogger;
import com.alttd.velocitychat.VelocityChat;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.Optional;
import java.util.UUID;

public class PluginMessageListener {

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
            case "globalchat" -> VelocityChat.getPlugin().getServerHandler().sendGlobalChat(in.readUTF(), in.readUTF());
            case "globaladminchat" -> VelocityChat.getPlugin().getChatHandler().globalAdminChat(in.readUTF());
            case "privatemessage" ->
                    VelocityChat.getPlugin().getChatHandler().privateMessage(in.readUTF(), in.readUTF(), in.readUTF());
            case "chatchannel" -> {
                String channelName = in.readUTF();
                CustomChannel chatChannel = (CustomChannel) CustomChannel.getChatChannel(channelName);

                if (chatChannel == null) {
                    ALogger.warn("Received non existent channel" + channelName + ".");
                    break;
                }

                ProxyServer proxy = VelocityChat.getPlugin().getProxy();
                chatChannel.getServers().forEach(server -> proxy.getServer(server).ifPresent(registeredServer ->
                        registeredServer.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), event.getData())));
            }
            case "party" -> {
                VelocityChat.getPlugin().getChatHandler().sendPartyMessage(
                        UUID.fromString(in.readUTF()),
                        in.readUTF(),
                        GsonComponentSerializer.gson().deserialize(in.readUTF()),
                        serverConnection);
            }

            // nicknames WIP
            case "NickNameAccepted", "NickNameSet" -> {
                try {
                    short len = in.readShort();
                    byte[] msgbytes = new byte[len];
                    in.readFully(msgbytes);

                    DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
                    UUID uuid = UUID.fromString(msgin.readUTF());
                    ChatUser chatUser = ChatUserManager.getChatUser(uuid);
                    chatUser.reloadDisplayName();

                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                ProxyServer proxy = VelocityChat.getPlugin().getProxy();
                proxy.getAllServers().forEach(registeredServer ->
                        registeredServer.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), event.getData()));
            }
            case "NickNameRequest", "NickNameDenied" -> {
                ProxyServer proxy = VelocityChat.getPlugin().getProxy();
                proxy.getAllServers().forEach(registeredServer ->
                        registeredServer.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), event.getData()));
            }
            case "punish" -> {
                String playerName = in.readUTF();
                ProxyServer proxy = VelocityChat.getPlugin().getProxy();
                ConsoleCommandSource consoleCommandSource = proxy.getConsoleCommandSource();
                proxy.getCommandManager().executeAsync(consoleCommandSource, String.format("ban %s Automatic ban, please appeal if you feel review is needed.", playerName));
                ALogger.info(String.format("Auto banned %s due to violating the `punish` filter.", playerName));
            }
            default -> {
                VelocityChat.getPlugin().getLogger().info("server " + event.getSource());
                ProxyServer proxy = VelocityChat.getPlugin().getProxy();
                for (RegisteredServer registeredServer : proxy.getAllServers()) {
                    registeredServer.sendPluginMessage(VelocityChat.getPlugin().getChannelIdentifier(), event.getData());
                }
            }
        }
    }

}
