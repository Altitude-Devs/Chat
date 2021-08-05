package com.alttd.chat.objects;

import java.util.*;

public class Channel {
    public static HashMap<String, Channel> channels = new HashMap<>();
    private String permission;
    private String channelName;
    private String format;
    private List<String> servers;
    private boolean proxy;

    public Channel(String channelName, String format, List<String> servers, boolean proxy) {
        this.permission = "chat.channel." + channelName.toLowerCase();
        this.channelName = channelName;
        this.format = format;
        this.servers = servers;
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

    public List<String> getServers() {
        return servers;
    }

    public boolean isProxy() {
        return proxy;
    }

    public static Channel getChatChannel(String channelName) {
        return channels.get(channelName.toLowerCase());
    }
}
