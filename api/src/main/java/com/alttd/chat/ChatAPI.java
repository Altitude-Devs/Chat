package com.alttd.chat;

import com.alttd.chat.database.DatabaseConnection;
import net.luckperms.api.LuckPerms;

import java.util.UUID;

public interface ChatAPI {

    ChatAPI get();

    LuckPerms getLuckPerms();

    DatabaseConnection getDataBase();

    String getPrefix(UUID uuid);

    String getPrefix(UUID uuid, boolean all);

    String getStaffPrefix(UUID uuid);

}
