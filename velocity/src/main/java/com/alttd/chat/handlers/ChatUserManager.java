package com.alttd.chat.handlers;

import com.alttd.chat.data.ChatUser;
import com.alttd.chat.data.Mail;
import com.alttd.chat.data.ServerWrapper;
import com.alttd.chat.database.Queries;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public final class ChatUserManager {

    private static CopyOnWriteArrayList<ChatUser> chatUsers;// not sure on this, could cause errors later on

    public static void initialize() {
        chatUsers = new CopyOnWriteArrayList<>();
        Queries.loadChatUsers();
    }

    public static void addUser(ChatUser user) {
        if(getChatUser(user.getUuid()) != null)
            chatUsers.add(user);
    }

    public static ChatUser getChatUser(UUID uuid) {
        for(ChatUser user : chatUsers) {
            if(user.getUuid() == uuid) {
                return user;
            }
        }
        return null;
    }

    public List<Mail> getUnReadMail(ChatUser user) {
        return user.getMails().stream()
                .filter(Mail::isUnRead)
                .collect(Collectors.toList());
    }
}
