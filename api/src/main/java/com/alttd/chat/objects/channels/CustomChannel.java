package com.alttd.chat.objects.channels;

import java.util.*;

public class CustomChannel extends Channel {
    private final List<String> servers;

    public CustomChannel(String channelName, String format, List<String> servers, boolean proxy) {
        super(channelName, format, proxy);
        this.permission = "chat.channel." + channelName.toLowerCase();
        this.channelName = channelName;
        this.format = format;
        this.servers = servers;
        this.proxy = proxy;
        channels.put(channelName.toLowerCase(), this);
    }
    public List<String> getServers() {
        return servers;
    }
}
