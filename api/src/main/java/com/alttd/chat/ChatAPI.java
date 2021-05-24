package com.alttd.chat;

import net.luckperms.api.LuckPerms;

import java.util.UUID;

public interface ChatAPI {

    static ChatAPI get() {
        return ChatImplementation.get();
    }

    LuckPerms getLuckPerms();

    String getPrefix(UUID uuid);

    String getPrefix(UUID uuid, boolean all);

    String getStaffPrefix(UUID uuid);

}
