package com.alttd.chat;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import java.util.UUID;

public class ChatImplementation implements ChatAPI{

    private static ChatAPI instance;

    private LuckPerms luckPerms;

    public ChatImplementation() {
        instance = this;

        luckPerms = getLuckPerms();

    }

    public static ChatAPI get() {
        if(instance == null)
            instance = new ChatImplementation();
        return instance;
    }

    @Override
    public LuckPerms getLuckPerms() {
        if(luckPerms == null)
            luckPerms = LuckPermsProvider.get();
        return luckPerms;
    }

    @Override
    public String getPrefix(UUID uuid) {
        return "";
    }

    @Override
    public String getPrefix(UUID uuid, boolean all) {
        return "";
    }

    @Override
    public String getStaffPrefix(UUID uuid) {
        return "";
    }


}
