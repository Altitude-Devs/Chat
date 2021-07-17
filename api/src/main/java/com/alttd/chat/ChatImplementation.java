package com.alttd.chat;

import com.alttd.chat.config.Config;
import com.alttd.chat.database.DatabaseConnection;
import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

public class ChatImplementation implements ChatAPI{

    private static ChatAPI instance;

    private LuckPerms luckPerms;
    private DatabaseConnection databaseConnection;

    public ChatImplementation() {
        instance = this;
        Config.init();

        luckPerms = getLuckPerms();
        databaseConnection = getDataBase();
        Queries.createTables();

        ChatUserManager.initialize(); // loads all the users from the db and adds them.
        RegexManager.initialize(); // load the filters and regexes from config
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
    public DatabaseConnection getDataBase() {
        if(databaseConnection == null)
            databaseConnection = new DatabaseConnection();
        return databaseConnection;
    }

}
