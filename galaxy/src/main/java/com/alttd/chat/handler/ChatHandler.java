package com.alttd.chat.handler;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
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

    // Start - move these to util
    public Component itemComponent(ItemStack item) {
        Component component = Component.text("[i]");
        if(item.getType().equals(Material.AIR))
            return component;
        boolean dname = item.hasItemMeta() && item.getItemMeta().hasDisplayName();
        if(dname) {
            component = component.append(item.getItemMeta().displayName());
        } else {
            component = Component.text(materialToName(item.getType()));
        }
        component = component.hoverEvent(item.asHoverEvent());
        return component;
    }

    private static String materialToName(Material m) {
        if (m.equals(Material.TNT)) {
            return "TNT";
        }
        String orig = m.toString().toLowerCase();
        String[] splits = orig.split("_");
        StringBuilder sb = new StringBuilder(orig.length());
        int pos = 0;
        for (String split : splits) {
            sb.append(split);
            int loc = sb.lastIndexOf(split);
            char charLoc = sb.charAt(loc);
            if (!(split.equalsIgnoreCase("of") || split.equalsIgnoreCase("and") ||
                    split.equalsIgnoreCase("with") || split.equalsIgnoreCase("on")))
                sb.setCharAt(loc, Character.toUpperCase(charLoc));
            if (pos != splits.length - 1)
                sb.append(' ');
            ++pos;
        }

        return sb.toString();
    }
    // end - move these to util
}
