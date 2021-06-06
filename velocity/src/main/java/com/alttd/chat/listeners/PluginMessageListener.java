package com.alttd.chat.listeners;

import com.alttd.chat.VelocityChat;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;

public class PluginMessageListener {

    //todo add an extra listener for nicknames?
    private final ChannelIdentifier identifier;

    public PluginMessageListener(ChannelIdentifier identifier){
        this.identifier = identifier;
    }

    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event){
        if(event.getIdentifier().equals(identifier)){
            event.setResult(PluginMessageEvent.ForwardResult.handled());

            if(event.getSource() instanceof Player){
                // if this happens there's an oopsie
            }
            if(event.getSource() instanceof ServerConnection){
                // Read the data written to the message
                ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
                String channel = in.readUTF();
                VelocityChat.getPlugin().getLogger().info("server " + event.getSource());
                switch (channel) {
                    case "globalchat":
                        // todo this is obsolete
                        //VelocityChat.getPlugin().getServerHandler().sendGlobalChat(in.readUTF());
                        break;
                    case "globaladminchat":
                        VelocityChat.getPlugin().getChatHandler().globalAdminChat(in.readUTF());
                        break;
                    default:
                        VelocityChat.getPlugin().getLogger().info("server " + event.getSource());
                        break;
                }
            }
        }
    }

}
