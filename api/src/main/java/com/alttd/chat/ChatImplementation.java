package com.alttd.chat;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

public class ChatImplementation implements ChatAPI{

    //public static final ChatAPI INSTANCE = new ChatImplementation();

    private LuckPerms luckPerms;

    ChatImplementation() {
        // init database
        // init depends//or set them the first time they are called?
    }

    @Override
    public LuckPerms getLuckPerms() {
        if(luckPerms == null)
            luckPerms = LuckPermsProvider.get();
        return luckPerms;
    }
}
