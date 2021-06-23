package com.alttd.chat.handlers;

import com.alttd.chat.VelocityChat;
import com.alttd.chat.config.Config;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.util.Utility;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

import java.util.*;

public class ChatHandler {

    public void privateMessage(String sender, String target, String message) {
        UUID uuid = UUID.fromString(sender);
        ChatUser senderUser = ChatUserManager.getChatUser(uuid);
        Optional<Player> optionalPlayer = VelocityChat.getPlugin().getProxy().getPlayer(uuid);
        if(optionalPlayer.isEmpty()) return;
        Player player = optionalPlayer.get();

        Optional<Player> optionalPlayer2 = VelocityChat.getPlugin().getProxy().getPlayer(target);
        if(optionalPlayer2.isEmpty()) return;
        Player player2 = optionalPlayer2.get();
        ChatUser targetUser = ChatUserManager.getChatUser(player2.getUniqueId());

        MiniMessage miniMessage = MiniMessage.get();

        List<Template> templates = new ArrayList<>(List.of(
                Template.of("sender", senderUser.getDisplayName()),
                Template.of("receiver", targetUser.getDisplayName()),
                Template.of("message", message),
                Template.of("server", player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Altitude")));

        Component component = miniMessage.parse("<message>", templates);

        Component senderMessage = miniMessage.parse(Config.MESSAGESENDER, templates);
        Component receiverMessage = miniMessage.parse(Config.MESSAGERECIEVER, templates);

        player.sendMessage(senderMessage);
        player2.sendMessage(receiverMessage);
    }

    public void globalAdminChat(String message) {
        Component component = GsonComponentSerializer.gson().deserialize(message);

        VelocityChat.getPlugin().getProxy().getAllPlayers().stream().filter(target -> target.hasPermission("command.proxy.globaladminchat")/*TODO permission*/).forEach(target -> {
            target.sendMessage(component);
        });
    }

    public void globalAdminChat(CommandSource commandSource, String message) {
        String senderName = Config.CONSOLENAME;
        String serverName = "Altitude";
        if (commandSource instanceof Player) {
            Player sender = (Player) commandSource;
            senderName = sender.getUsername();
            serverName = sender.getCurrentServer().isPresent() ? sender.getCurrentServer().get().getServerInfo().getName() : "Altitude";
        }

        MiniMessage miniMessage = MiniMessage.get();

        message = Utility.parseColors(message);

        Map<String, String> map = new HashMap<>();

        map.put("sender", senderName);
        //map.put("message", event.getMessage());
        map.put("message", Utility.parseColors(message));
        map.put("server", serverName);

        Component component = miniMessage.parse(Config.GACFORMAT, map);

        VelocityChat.getPlugin().getProxy().getAllPlayers().stream().filter(target -> target.hasPermission("command.proxy.globaladminchat")/*TODO permission*/).forEach(target -> {
            target.sendMessage(component);
        });
    }

    /**
     * constructs a mail object and notifies all involved players about it
     * / mail send playerA,playerB,playerC message
     */
    public void sendMail(CommandSource commandSource, String recipient, String message) {
        // todo construct the mail and notify the player if online?
    }

    /**
     * @param message the messaged to be parsed by the chatfilter
     * @param player the player invoking the message
     * @return the message altered by the filters
     */
    public String applyChatFilters(String message, Player player) {
        return "";
    }


    public void readMail(CommandSource commandSource, String targetPlayer, boolean unread) {

    }

    public void readMail(CommandSource commandSource, boolean unread) {

    }


}
