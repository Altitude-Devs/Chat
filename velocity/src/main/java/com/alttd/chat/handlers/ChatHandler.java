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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatHandler {

    public void globalChat(Player player, String message) {
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        if(user == null) return;
        MiniMessage miniMessage = MiniMessage.get();
        if(!user.isGcOn()) {
            player.sendMessage(miniMessage.parse(Config.GCNOTENABLED));// GC IS OFF INFORM THEM ABOUT THIS and cancel
            return;
        }

        message = Utility.parseColors(message);
        if(!player.hasPermission("chat.format")) // Todo PR fix for '<3' to minimessage
            message = miniMessage.stripTokens(message);

        message = RegexManager.replaceText(message); // this filters the message TODO should staff be able to bypass filters?

        List<Template> templates = new ArrayList<>(List.of(
                Template.of("sender", user.getDisplayName()),
                Template.of("prefix", user.getPrefix()),
                Template.of("message", message),
                Template.of("server", player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Altitude")
                /*,Template.of("[i]", itemComponent(sender.getInventory().getItemInMainHand()))*/ //Todo move this into ChatFilters
        ));

        Component component = miniMessage.parse(Config.GCFORMAT, templates);

        VelocityChat.getPlugin().getServerHandler().sendGlobalChat(component);

    }

    public void privateMessage(CommandSource commandSource, Player recipient, String message) {
        // todo get the chatUserinstance of both players and or console - @teri should console be able to /msg?
        String senderName;
        String receiverName;
        if (commandSource instanceof Player) {
            Player sender = (Player) commandSource;
            senderName = sender.getUsername();
            //plugin.getChatHandler().getChatPlayer(sender.getUniqueId()).setReplyTarget(event.getRecipient().getUniqueId()); // TODO this needs to be cleaner
        } else {
            senderName = Config.CONSOLENAME;
        }
        receiverName = recipient.getUsername();

        MiniMessage miniMessage = MiniMessage.get();

        message = Utility.parseColors(message);
        if(!commandSource.hasPermission("chat.format")) // Todo PR fix for '<3' to minimessage
            message = miniMessage.stripTokens(message);

        message = RegexManager.replaceText(message); // this filters the message TODO should staff be able to bypass filters?

//        List<Template> templates = new ArrayList<>(List.of(
//                Template.of("sender", user.getDisplayName()),
//                Template.of("prefix", user.getPrefix()),
//                Template.of("message", message),
//                Template.of("server", player.getCurrentServer().isPresent() ? player.getCurrentServer().get().getServerInfo().getName() : "Altitude")
//                /*,Template.of("[i]", itemComponent(sender.getInventory().getItemInMainHand()))*/ //Todo move this into ChatFilters
//        ));

        Map<String, String> map = new HashMap<>();

        map.put("sender", senderName);
        map.put("receiver", receiverName);
        map.put("message", message);
//        map.put("server", event.getRecipient().getCurrentServer().isPresent() ? event.getRecipient().getCurrentServer().get().getServerInfo().getName() : "Altitude");

        Component senderMessage = miniMessage.parse(Config.MESSAGESENDER, map);
        Component receiverMessage = miniMessage.parse(Config.MESSAGERECIEVER, map);

        commandSource.sendMessage(senderMessage);
        recipient.sendMessage(receiverMessage);
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
