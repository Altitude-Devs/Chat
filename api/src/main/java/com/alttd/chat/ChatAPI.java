package com.alttd.chat;

import net.luckperms.api.LuckPerms;

public interface ChatAPI {

    /*public static ChatAPI get() {
        return ChatImplementation.INSTANCE;
    }*/

    LuckPerms getLuckPerms();
}
