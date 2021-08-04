package com.alttd.chat.managers;

import com.alttd.chat.database.Queries;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Mail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public final class ChatUserManager {

    private static ArrayList<ChatUser> chatUsers;

    public static void initialize() {
        chatUsers = new ArrayList<>();
        loadUsers();
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

    public static void loadUsers() {
        Queries.loadChatUsers();
    }

    protected static List<ChatUser> getChatUsers() {
        return Collections.unmodifiableList(chatUsers);
    }
}
