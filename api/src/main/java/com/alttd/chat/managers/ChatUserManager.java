package com.alttd.chat.managers;

import com.alttd.chat.database.Queries;
import com.alttd.chat.objects.ChatUser;

import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public final class ChatUserManager {

    private static Map<UUID, ChatUser> chatUsers;

    public static void initialize() {
        chatUsers = new TreeMap<>();
    }

    public static synchronized void addUser(ChatUser user) {
        chatUsers.put(user.getUuid(), user);
    }

    public static synchronized void removeUser(ChatUser user) {
        chatUsers.remove(user.getUuid());
    }

    public static synchronized ChatUser getChatUser(UUID uuid) {
        return chatUsers.computeIfAbsent(uuid, k -> Queries.loadChatUser(uuid));
    }

}
