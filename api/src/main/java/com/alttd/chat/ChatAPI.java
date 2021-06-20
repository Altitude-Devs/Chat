package com.alttd.chat;

import com.alttd.chat.database.DatabaseConnection;
import net.luckperms.api.LuckPerms;

public interface ChatAPI {

    static ChatAPI get() {
        return ChatImplementation.get();
    }

    LuckPerms getLuckPerms();

    DatabaseConnection getDataBase();

}
