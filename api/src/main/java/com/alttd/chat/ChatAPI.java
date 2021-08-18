package com.alttd.chat;

import com.alttd.chat.database.DatabaseConnection;
import net.luckperms.api.LuckPerms;

import java.util.HashMap;

public abstract interface ChatAPI {

    static ChatAPI get() {
        return ChatImplementation.get();
    }

    LuckPerms getLuckPerms();

    DatabaseConnection getDataBase();

    void ReloadConfig();

    void ReloadChatFilters();

    HashMap<String, String> getPrefixes();

}
