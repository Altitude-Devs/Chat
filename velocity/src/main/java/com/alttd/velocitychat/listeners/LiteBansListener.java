package com.alttd.velocitychat.listeners;

import com.alttd.velocitychat.VelocityChat;
import litebans.api.Entry;
import litebans.api.Events;

public class LiteBansListener extends Events.Listener {

    public void init() {
        Events.get().register(this);
    }

    @Override
    public void entryAdded(Entry entry) {
        if (!entry.getType().equals("mute")) return;
        String uuid = entry.getUuid();
        if (uuid == null) return; // sanity check
        VelocityChat.getPlugin().getChatHandler().mutePlayer(uuid, entry.isActive());
    }

    @Override
    public void entryRemoved(Entry entry) {
        if (!entry.getType().equals("mute")) return;
        String uuid = entry.getUuid();
        if (uuid == null) return; // sanity check
        VelocityChat.getPlugin().getChatHandler().mutePlayer(uuid, entry.isActive());
    }
}
