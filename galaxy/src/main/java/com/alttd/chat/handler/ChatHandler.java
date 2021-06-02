package com.alttd.chat.handler;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.util.Utility;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ChatHandler {

    private ChatPlugin plugin;

    public ChatHandler() {
        plugin = ChatPlugin.getInstance();
    }

    public void globalChat(Player player, String message) {
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        if(user == null) return;
        if(!user.isGcOn()) {
            player.sendMessage();// GC IS OFF INFORM THEM ABOUT THIS and cancel
            return;
        }
        // Check if the player has global chat enabled, if not warn them
        String senderName, prefix = "";

        senderName = player.getDisplayName();   // TODO this can be a component
                                                // can also be cached in the chatuser object?
        prefix = plugin.getChatAPI().getPrefix(player.getUniqueId());

        MiniMessage miniMessage = MiniMessage.get();
        message = Utility.parseColors(message);
        if(!player.hasPermission("chat.format"))
            message = miniMessage.stripTokens(message);
        if(message.contains("[i]"))
            message = message.replace("[i]", "<[i]>");

        List<Template> templates = new ArrayList<>(List.of(
                Template.of("sender", senderName),
                Template.of("prefix", prefix),
                Template.of("message", message),
                Template.of("server", Bukkit.getServerName())/*,
                Template.of("[i]", itemComponent(sender.getInventory().getItemInMainHand()))*/));

        Component component = miniMessage.parse(Config.GCFORMAT, templates);

        //todo make a method for this, it'll be used more then onc

        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("globalchat");
        out.writeUTF(miniMessage.serialize(component));
        player.sendPluginMessage(plugin, Config.MESSAGECHANNEL, out.toByteArray());
    }


}
