package com.alttd.chat;

import com.alttd.chat.config.Config;
import com.alttd.chat.database.DatabaseConnection;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;

import java.io.File;

public class ChatImplementation implements ChatAPI{

    private ChatAPI instance;

    private LuckPerms luckPerms;
    private DatabaseConnection databaseConnection;

    ChatImplementation() {
        instance = this;
        Config.init(new File(System.getProperty("user.home")));
        // init database
        // init depends//or set them the first time they are called?
    }

    @Override
    public ChatAPI get() {
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
