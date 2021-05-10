package com.alttd.chat;

import com.alttd.chat.database.DatabaseConnection;
import net.luckperms.api.LuckPerms;

public interface ChatAPI {

    ChatAPI get();

    LuckPerms getLuckPerms();

    DatabaseConnection getDataBase();
}
