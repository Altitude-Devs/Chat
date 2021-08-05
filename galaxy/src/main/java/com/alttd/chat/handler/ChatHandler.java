package com.alttd.chat.handler;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.commands.ChatChannel;
import com.alttd.chat.config.Config;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.Channel;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.util.GalaxyUtility;
import com.alttd.chat.util.Utility;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import litebans.api.Database;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.StringUtil;

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
        String updatedMessage = RegexManager.replaceText(player.getName(), player.getUniqueId(), message); // todo a better way for this
        if(updatedMessage == null) {
            GalaxyUtility.sendBlockedNotification("DM Language", player, message, target);
            return; // the message was blocked
        }

        if(!player.hasPermission("chat.format")) {
            updatedMessage = miniMessage.stripTokens(updatedMessage);
        } else {
            updatedMessage = Utility.parseColors(updatedMessage);
        }

        if(updatedMessage.contains("[i]"))
            updatedMessage = updatedMessage.replace("[i]", "<[i]>");

        List<Template> templates = new ArrayList<>(List.of(
                Template.of("message", updatedMessage),
                Template.of("sendername", player.getName()),
                Template.of("receivername", target),
                Template.of("[i]", itemComponent(player.getInventory().getItemInMainHand()))));

        Component component = miniMessage.parse("<message>", templates);

        sendPrivateMessage(player, target, "privatemessage", component);
        Component spymessage = miniMessage.parse(Config.MESSAGESPY, templates);
        for(Player pl : Bukkit.getOnlinePlayers()) {
            if(pl.hasPermission(Config.SPYPERMISSION) && !pl.equals(player) && !pl.getName().equalsIgnoreCase(target)) { // todo add a toggle for social spy
                pl.sendMessage(spymessage);
            }
        }
    }

    public void globalChat(Player player, String message) {
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        if(!Utility.hasPermission(player.getUniqueId(), Config.GCPERMISSION)) {
            player.sendMessage(GCNOTENABLED);// GC IS OFF INFORM THEM ABOUT THIS and cancel
            return;
        }

        if (isMuted(player, message, "[GC Muted] ")) {
            return;
        }

        long timeLeft = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - user.getGcCooldown());
        if(timeLeft <= Config.GCCOOLDOWN && !player.hasPermission("chat.globalchat.cooldownbypass")) { // player is on cooldown and should wait x seconds
            player.sendMessage(miniMessage.parse(Config.GCONCOOLDOWN, Template.of("cooldown", Config.GCCOOLDOWN-timeLeft+"")));
            return;
        }

        Component senderName = user.getDisplayName();
        Component prefix = user.getPrefix();

        String updatedMessage = RegexManager.replaceText(player.getName(), player.getUniqueId(), message); // todo a better way for this
        if(updatedMessage == null) {
            GalaxyUtility.sendBlockedNotification("GC Language", player, message, "");
            return; // the message was blocked
        }

        if(!player.hasPermission("chat.format")) {
            updatedMessage = miniMessage.stripTokens(updatedMessage);
        } else {
            updatedMessage = Utility.parseColors(updatedMessage);
        }

        if(updatedMessage.contains("[i]"))
            updatedMessage = updatedMessage.replace("[i]", "<[i]>"); // end of todo

        List<Template> templates = new ArrayList<>(List.of(
                Template.of("sender", senderName),
                Template.of("prefix", prefix),
                Template.of("message", updatedMessage),
                Template.of("server", Bukkit.getServerName()),
                Template.of("[i]", itemComponent(player.getInventory().getItemInMainHand()))));

        Component component = miniMessage.parse(Config.GCFORMAT, templates);
        user.setGcCooldown(System.currentTimeMillis());
        sendPluginMessage(player, "globalchat", component);
    }

    public void chatChannel(Player player, Channel channel, String message) {
        if (!player.hasPermission(channel.getPermission())) {
            player.sendMessage(MiniMessage.get().parse("<red>You don't have permission to use this channel.</red>"));
            return;
        }

        if (isMuted(player, message, "[" + channel.getChannelName() + " Muted] ")) return;

        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        Component senderName = user.getDisplayName();

        String updatedMessage = RegexManager.replaceText(player.getName(), player.getUniqueId(), message);
        if(updatedMessage == null) {
            GalaxyUtility.sendBlockedNotification("GC Language", player, message, "");
            return; // the message was blocked
        }

        if(!player.hasPermission("chat.format")) {
            updatedMessage = miniMessage.stripTokens(updatedMessage);
        }

        if(updatedMessage.contains("[i]")) updatedMessage = updatedMessage.replace("[i]", "<[i]>");

        List<Template> templates = new ArrayList<>(List.of(
                Template.of("sender", senderName),
                Template.of("message", updatedMessage),
                Template.of("server", Bukkit.getServerName()),
                Template.of("channel", channel.getChannelName()),
                Template.of("[i]", itemComponent(player.getInventory().getItemInMainHand()))));

        Component component = miniMessage.parse(channel.getFormat(), templates);

        if (channel.isProxy()) {
            sendChatChannelMessage(player, channel.getChannelName(), "chatchannel", component);
        } else {
            sendChatChannelMessage(channel, component);
        }
    }

    private void sendChatChannelMessage(Channel chatChannel, Component component) {
        if (!chatChannel.getServers().contains(Bukkit.getServerName())) return;

        Bukkit.getServer().getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(chatChannel.getPermission()))
                .forEach(p -> p.sendMessage(component));
    }

    private void sendPluginMessage(Player player, String channel, Component component) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(channel);
        out.writeUTF(player.getUniqueId().toString());
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

    public void sendChatChannelMessage(Player player, String chatChannelName, String channel, Component component) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF(channel);
        out.writeUTF(chatChannelName);
        out.writeUTF(GsonComponentSerializer.gson().serialize(component));
        player.sendPluginMessage(plugin, Config.MESSAGECHANNEL, out.toByteArray());
    }
    // Start - move these to util

    private boolean isMuted(Player player, String message, String prefix) {
        if (Database.get().isPlayerMuted(player.getUniqueId(), null) || (ChatPlugin.getInstance().serverMuted() && !player.hasPermission("chat.bypass-server-muted"))) {
            MiniMessage miniMessage = MiniMessage.get();
            Component blockedNotification = miniMessage.parse("<red>" + prefix
                    + Utility.getDisplayName(player.getUniqueId(), player.getName())
                    + " tried to say: "
                    + message + "</red>");

            Bukkit.getOnlinePlayers().forEach(a ->{
                if (a.hasPermission("chat.alert-blocked")) {
                    a.sendMessage(blockedNotification);//TODO make configurable (along with all the messages)
                }
            });
            return true;
        }
        return false;
    }

    public static Component itemComponent(ItemStack item) {
        Component component = Component.text("[i]", NamedTextColor.AQUA);
        if(item.getType().equals(Material.AIR))
            return component.color(NamedTextColor.WHITE);
        boolean dname = item.hasItemMeta() && item.getItemMeta().hasDisplayName();
        if(dname) {
            component = component.append(item.getItemMeta().displayName());
        } else {
            component = component.append(Component.text(materialToName(item.getType()), NamedTextColor.WHITE));
        }
        component = component.append(Component.text(" x" + item.getAmount(), NamedTextColor.AQUA));
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
