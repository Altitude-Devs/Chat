package com.alttd.chat.managers;

import com.alttd.chat.database.Queries;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Mail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
        // todo move the CompletableFuture to Queries
        return CompletableFuture.supplyAsync(() -> {
            ChatUser loadChatUser = Queries.loadChatUser(uuid);
            if (loadChatUser != null) {
                ChatUserManager.addUser(loadChatUser);
            }
            return loadChatUser;
        }).join();
//        if(user == null) user = new ChatUser(uuid, -1, false, false);
//        Queries.saveUser(user);
//        chatUsers.add(user);
//        return user;
    }

    public List<Mail> getUnReadMail(ChatUser user) {
        return user.getMails().stream()
                .filter(Mail::isUnRead)
                .collect(Collectors.toList());
    }
}
