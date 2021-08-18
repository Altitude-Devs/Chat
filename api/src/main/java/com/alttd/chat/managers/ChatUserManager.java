package com.alttd.chat.managers;

import com.alttd.chat.database.Queries;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Mail;

import java.util.*;
import java.util.stream.Collectors;

public final class ChatUserManager {

    private static ArrayList<ChatUser> chatUsers;

    public static void initialize() {
        chatUsers = new ArrayList<>();
    }

    public static void addUser(ChatUser user) {
        chatUsers.add(user);
    }

    public static void removeUser(ChatUser user) {
        chatUsers.remove(user);
    }

    public static ChatUser getChatUser(UUID uuid) {
        for(ChatUser user : chatUsers) {
            if(uuid.equals(user.getUuid())) {
                return user;
            }
        }
        return Queries.loadChatUser(uuid);
    }

    public List<Mail> getUnReadMail(ChatUser user) {
        return user.getMails().stream()
                .filter(Mail::isUnRead)
                .collect(Collectors.toList());
    }

    protected static List<ChatUser> getChatUsers() {
        return Collections.unmodifiableList(chatUsers);
    }
}
