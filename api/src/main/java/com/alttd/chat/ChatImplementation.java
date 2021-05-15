package com.alttd.chat;

import com.alttd.chat.config.Config;
import com.alttd.chat.database.DatabaseConnection;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;

public class ChatImplementation implements ChatAPI{

    private static ChatAPI instance;

    private LuckPerms luckPerms;
    private DatabaseConnection databaseConnection;

    public ChatImplementation() {
        instance = this;
        Config.init();

        luckPerms = getLuckPerms();
        databaseConnection = getDataBase();
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
    public String getPrefix(UUID uuid) {
        return getPrefix(uuid, false);
    }

    @Override
    public String getPrefix(UUID uuid, boolean all) {
        // TODO cache these components on load, and return them here?
        StringBuilder prefix = new StringBuilder();
        LuckPerms luckPerms = getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        if(user == null) return "";
        if(all) {
            Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());
            inheritedGroups.stream()
                    .sorted(Comparator.comparingInt(o -> o.getWeight().orElse(0)))
                    .forEach(group -> {
                        if (Config.PREFIXGROUPS.contains(group.getName())) {
                            prefix.append("<white>[").append(group.getCachedData().getMetaData().getPrefix()).append("]</white>");
                        }
                    });
        }
        prefix.append("<white>[").append(user.getCachedData().getMetaData().getPrefix()).append("]</white>");
        return prefix.toString();
    }

    @Override
    public String getStaffPrefix(UUID uuid) {
        return "";
    }


}
