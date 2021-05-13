package com.alttd.chat.listeners;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;

public class PluginMessageListener {

    private final ChannelIdentifier identifier;

    public PluginMessageListener(ChannelIdentifier identifier){
        this.identifier = identifier;
    }

    @Subscribe
    public void onPluginMessageEvent(PluginMessageEvent event){
        if(event.getIdentifier().equals(identifier)){
            event.setResult(PluginMessageEvent.ForwardResult.handled());

            if(event.getSource() instanceof Player){

            }
            if(event.getSource() instanceof ServerConnection){
                // Read the data written to the message
                ByteArrayDataInput in = ByteStreams.newDataInput(event.getData());
                String message = in.readUTF();

            }
        }
    }

}
