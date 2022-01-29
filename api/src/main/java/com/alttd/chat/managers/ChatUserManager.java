package com.alttd.chat.managers;

import com.alttd.chat.database.Queries;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Mail;

import java.util.*;
import java.util.stream.Collectors;

public final class ChatUserManager {

    private static Map<UUID, ChatUser> chatUsers;

    public static void initialize() {
        chatUsers = new TreeMap<>();
    }

    public static void addUser(ChatUser user) {
        chatUsers.put(user.getUuid(), user);
    }

    public static void removeUser(ChatUser user) {
        chatUsers.remove(user.getUuid());
    }

    public static ChatUser getChatUser(UUID uuid) {
        return chatUsers.computeIfAbsent(uuid, k -> Queries.loadChatUser(uuid));
    }

}
