package com.alttd.chat;

import com.alttd.chat.config.Config;
import com.alttd.chat.database.DatabaseConnection;
import com.alttd.chat.database.Queries;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.managers.RegexManager;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

public class ChatImplementation implements ChatAPI{

    private static ChatAPI instance;

    private LuckPerms luckPerms;
    private DatabaseConnection databaseConnection;

    public ChatImplementation() {
        instance = this;
        ReloadConfig();

        luckPerms = getLuckPerms();
        databaseConnection = getDataBase();
        Queries.createTables();

        ChatUserManager.initialize(); // loads all the users from the db and adds them.
        RegexManager.initialize(); // load the filters and regexes from config
        PartyManager.initialize(); // load the parties from the db and add the previously loaded users to them
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

    @Override
    public void ReloadConfig() {
        Config.init();
    }

    @Override
    public void ReloadChatFilters() {
        RegexManager.initialize();
    }

}
