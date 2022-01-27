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
        chatUsers.remove(user);
    }

    /**
     * Get the ChatUser for this player or query the database to read the data.
     *
     * @param uuid the player who's ChatUser you'd like to get
     * @return The ChatUser loaded from database or null if it's not existing.
     */
    public static ChatUser getChatUser(UUID uuid) {
        return chatUsers.computeIfAbsent(uuid, k -> Queries.loadChatUser(uuid));
    }

    public List<Mail> getUnReadMail(ChatUser user) {
        return user.getMails().stream()
                .filter(Mail::isUnRead)
                .collect(Collectors.toList());
    }

}
