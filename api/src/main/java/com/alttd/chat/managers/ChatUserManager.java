package com.alttd.chat.managers;

import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Mail;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class ChatUserManager {

    private static ArrayList<ChatUser> chatUsers;// not sure on this, could cause errors later on

    public static void initialize() {
        chatUsers = new ArrayList<>();
        //Queries.loadChatUsers(); // todo fix sql
    }

    public static void addUser(ChatUser user) {
        if(getChatUser(user.getUuid()) == null)
            chatUsers.add(user);
    }

    public static ChatUser getChatUser(UUID uuid) {
        for(ChatUser user : chatUsers) {
            if(uuid.compareTo(user.getUuid()) == 0) {
                return user;
            }
        }
        ChatUser user = new ChatUser(uuid, -1, false, false);
        chatUsers.add(user);
        return user; // create a new user?
    }

    public List<Mail> getUnReadMail(ChatUser user) {
        return user.getMails().stream()
                .filter(Mail::isUnRead)
                .collect(Collectors.toList());
    }
}
