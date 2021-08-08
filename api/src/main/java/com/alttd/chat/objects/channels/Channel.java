package com.alttd.chat.objects.channels;

import java.util.Collection;
import java.util.HashMap;

public abstract class Channel {

    public static HashMap<String, Channel> channels = new HashMap<>();
    protected String permission;
    protected String channelName;
    protected String format;
    protected boolean proxy;

    public Channel(String channelName, String format, boolean proxy) {
        this.permission = "chat.channel." + channelName.toLowerCase();
        this.channelName = channelName;
        this.format = format;
        this.proxy = proxy;
        channels.put(channelName.toLowerCase(), this);
    }

    public static Collection<Channel> getChannels() {
        return channels.values();
    }

    public String getPermission() {
        return permission;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getFormat() {
        return format;
    }

    public boolean isProxy() {
        return proxy;
    }

    public static Channel getChatChannel(String channelName) {
        return channels.get(channelName.toLowerCase());
    }
}
