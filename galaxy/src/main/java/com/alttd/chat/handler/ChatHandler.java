package com.alttd.chat.handler;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.util.Utility;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChatHandler {

    private final ChatPlugin plugin;

    private final MiniMessage miniMessage;
    private final Component GCNOTENABLED;

    public ChatHandler() {
        plugin = ChatPlugin.getInstance();
        miniMessage = MiniMessage.get();
        GCNOTENABLED = miniMessage.parse(Config.GCNOTENABLED);
    }

    public void privateMessage(Player player, String target, String message) {
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        user.setReplyTarget(target);
        message = RegexManager.replaceText(message); // todo a better way for this
        if(message == null) return; // the message was blocked

        if(!player.hasPermission("chat.format")) {
            message = miniMessage.stripTokens(message);
        } else {
            message = Utility.parseColors(message);
        }

        if(message.contains("[i]"))
            message = message.replace("[i]", "<[i]>");

        List<Template> templates = new ArrayList<>(List.of(
                Template.of("message", message),
                Template.of("[i]", itemComponent(player.getInventory().getItemInMainHand())))); // yes cross server [i];)

        Component component = miniMessage.parse("<message>", templates);

        sendPrivateMessage(player, target, "privatemessage", component);
    }

    public void globalChat(Player player, String message) {
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        if(user == null) return;
        if(!user.isGcOn()) {
            player.sendMessage(GCNOTENABLED);// GC IS OFF INFORM THEM ABOUT THIS and cancel
            return;
        }
        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - user.getGcCooldown());
        if(timeLeft <= Config.GCCOOLDOWN) { // player is on cooldown and should wait x seconds
            player.sendMessage(miniMessage.parse(Config.GCONCOOLDOWN, Template.of("cooldown", timeLeft+"")));
            return;
        }

        Component senderName = player.displayName();
        String prefix = user.getPrefix();

        message = RegexManager.replaceText(message); // todo a better way for this
        if(message == null) return; // the message was blocked

        if(!player.hasPermission("chat.format")) {
            message = miniMessage.stripTokens(message);
        } else {
            message = Utility.parseColors(message);
        }

        if(message.contains("[i]"))
            message = message.replace("[i]", "<[i]>"); // end of todo

        List<Template> templates = new ArrayList<>(List.of(
                Template.of("sender", senderName),
                Template.of("prefix", prefix),
                Template.of("message", message),
                Template.of("server", Bukkit.getServerName()),
                Template.of("[i]", itemComponent(player.getInventory().getItemInMainHand()))));

        Component component = miniMessage.parse(Config.GCFORMAT, templates);

        sendPluginMessage(player, "globalchat", component);
    }

    private void sendPluginMessage(Player player, String channel, Component component) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(channel);
        out.writeUTF(GsonComponentSerializer.gson().serialize(component));
        player.sendPluginMessage(plugin, Config.MESSAGECHANNEL, out.toByteArray());
    }

    private void sendPrivateMessage(Player player, String target, String channel, Component component) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(channel);
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(target);
        out.writeUTF(GsonComponentSerializer.gson().serialize(component));
        player.sendPluginMessage(plugin, Config.MESSAGECHANNEL, out.toByteArray());
    }

    // Start - move these to util
    public static Component itemComponent(ItemStack item) {
        Component component = Component.text("[i]");
        if(item.getType().equals(Material.AIR)) // do we want to show the <players hand>?
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
