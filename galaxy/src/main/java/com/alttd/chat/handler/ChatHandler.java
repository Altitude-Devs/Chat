package com.alttd.chat.handler;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.channels.CustomChannel;
import com.alttd.chat.util.GalaxyUtility;
import com.alttd.chat.util.Utility;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.minimessage.placeholder.Replacement;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChatHandler {

    private final ChatPlugin plugin;

    private final Component GCNOTENABLED;

    public ChatHandler() {
        plugin = ChatPlugin.getInstance();
        GCNOTENABLED = Utility.parseMiniMessage(Config.GCNOTENABLED);
    }

    public void privateMessage(Player player, String target, String message) {
//        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
//        user.setReplyTarget(target);
        String updatedMessage = RegexManager.replaceText(player.getName(), player.getUniqueId(), message); // todo a better way for this
        if(updatedMessage == null) {
            GalaxyUtility.sendBlockedNotification("DM Language", player, message, target);
            return; // the message was blocked
        }

        if(!player.hasPermission("chat.format")) {
            updatedMessage = Utility.stripTokens(updatedMessage);
        } else {
            updatedMessage = Utility.parseColors(updatedMessage);
        }

        if(updatedMessage.contains("[i]"))
            updatedMessage = updatedMessage.replace("[i]", "<[i]>");

        updatedMessage = Utility.formatText(updatedMessage);

        Map<String, Replacement<?>> placeholders = new HashMap<>();
        placeholders.put("message", Replacement.miniMessage(updatedMessage));
        placeholders.put("sendername", Replacement.miniMessage(player.getName()));
        placeholders.put("receivername", Replacement.miniMessage(target));
        placeholders.put("[i]", Replacement.component(itemComponent(player.getInventory().getItemInMainHand())));

        Component component = Utility.parseMiniMessage("<message>", placeholders);

        sendPrivateMessage(player, target, "privatemessage", component);
        Component spymessage = Utility.parseMiniMessage(Config.MESSAGESPY, placeholders);
        for(Player pl : Bukkit.getOnlinePlayers()) {
            if(pl.hasPermission(Config.SPYPERMISSION) && ChatUserManager.getChatUser(pl.getUniqueId()).isSpy() && !pl.equals(player) && !pl.getName().equalsIgnoreCase(target)) {
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
            Map<String, Replacement<?>> placeholders = new HashMap<>();
            placeholders.put("cooldown", Replacement.miniMessage(Config.GCCOOLDOWN-timeLeft+""));
            player.sendMessage(Utility.parseMiniMessage(Config.GCONCOOLDOWN, placeholders));
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
            updatedMessage = Utility.stripTokens(updatedMessage);
        } else {
            updatedMessage = Utility.parseColors(updatedMessage);
        }

        if(updatedMessage.contains("[i]"))
            updatedMessage = updatedMessage.replace("[i]", "<[i]>");

        updatedMessage = Utility.formatText(updatedMessage);

        Map<String, Replacement<?>> placeholders = new HashMap<>();
        placeholders.put("sender", Replacement.component(senderName));
        placeholders.put("prefix", Replacement.component(prefix));
        placeholders.put("message", Replacement.miniMessage(updatedMessage));
        placeholders.put("server", Replacement.miniMessage(Bukkit.getServerName()));
        placeholders.put("[i]", Replacement.component(itemComponent(player.getInventory().getItemInMainHand())));

        Component component = Utility.parseMiniMessage(Config.GCFORMAT, placeholders);
        user.setGcCooldown(System.currentTimeMillis());
        sendPluginMessage(player, "globalchat", component);
    }

    public void chatChannel(Player player, CustomChannel channel, String message) {
        if (!player.hasPermission(channel.getPermission())) {
            player.sendMessage(Utility.parseMiniMessage("<red>You don't have permission to use this channel.</red>"));
            return;
        }

        if (isMuted(player, message, "[" + channel.getChannelName() + " Muted] ")) return;

        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        Component senderName = user.getDisplayName();

        String updatedMessage = RegexManager.replaceText(player.getName(), player.getUniqueId(), message);
        if(updatedMessage == null) {
            GalaxyUtility.sendBlockedNotification(channel.getChannelName() + " Language", player, message, "");
            return; // the message was blocked
        }

        if(!player.hasPermission("chat.format")) {
            updatedMessage = Utility.stripTokens(updatedMessage);
        }

        if(updatedMessage.contains("[i]")) updatedMessage = updatedMessage.replace("[i]", "<[i]>");

        updatedMessage = Utility.formatText(updatedMessage);

        Map<String, Replacement<?>> placeholders = new HashMap<>();
        placeholders.put("sender", Replacement.component(senderName));
        placeholders.put("message", Replacement.miniMessage(updatedMessage));
        placeholders.put("server", Replacement.miniMessage(Bukkit.getServerName()));
        placeholders.put("channel", Replacement.miniMessage(channel.getChannelName()));
        placeholders.put("[i]", Replacement.component(itemComponent(player.getInventory().getItemInMainHand())));

        Component component = Utility.parseMiniMessage(channel.getFormat(), placeholders);

        if (channel.isProxy()) {
            sendChatChannelMessage(player, channel.getChannelName(), "chatchannel", component);
        } else {
            sendChatChannelMessage(channel, player.getUniqueId(), component);
        }
    }

    public void partyMessage(Player player, String message) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("party");
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(message);
        out.writeUTF(GsonComponentSerializer.gson().serialize(
                itemComponent(player.getInventory().getItemInMainHand())
        ));
        player.sendPluginMessage(plugin, Config.MESSAGECHANNEL, out.toByteArray());


//        if (isMuted(player, message, "[" + party.getPartyName() + " Muted] ")) return;
//
//        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
//        Component senderName = user.getDisplayName();
//
//        String updatedMessage = RegexManager.replaceText(player.getName(), player.getUniqueId(), message);
//        if(updatedMessage == null) {
//            GalaxyUtility.sendBlockedNotification("Party Language", player, message, "");
//            return; // the message was blocked
//        }
//
//        if(!player.hasPermission("chat.format")) {
//            updatedMessage = Utility.stripTokens(updatedMessage);
//        }
//
//        if(updatedMessage.contains("[i]")) updatedMessage = updatedMessage.replace("[i]", "<[i]>");
//
//        updatedMessage = Utility.formatText(updatedMessage);
//
//        List<Template> templates = new ArrayList<>(List.of(
//                Placeholder.miniMessage("sender", senderName),
//                Placeholder.miniMessage("sendername", senderName),
//                Placeholder.miniMessage("partyname", party.getPartyName()),
//                Placeholder.miniMessage("message", updatedMessage),
//                Placeholder.miniMessage("server", Bukkit.getServerName()),
//                Placeholder.miniMessage("[i]", itemComponent(player.getInventory().getItemInMainHand()))));
//
//        Component component = Utility.parseMiniMessage(Config.PARTY_FORMAT, templates);
////        sendPartyMessage(player, party.getPartyId(), component);
//
//        Component spyMessage = Utility.parseMiniMessage(Config.PARTY_SPY, templates);
//        for(Player pl : Bukkit.getOnlinePlayers()) {
//            if(pl.hasPermission(Config.SPYPERMISSION) && !party.getPartyUsersUuid().contains(pl.getUniqueId())) {
//                pl.sendMessage(spyMessage);
//            }
//        }
    }

    private void sendChatChannelMessage(CustomChannel chatChannel, UUID uuid, Component component) {
        if (!chatChannel.getServers().contains(Bukkit.getServerName())) return;

        Bukkit.getServer().getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(chatChannel.getPermission()))
                .filter(p -> !ChatUserManager.getChatUser(p.getUniqueId()).getIgnoredPlayers().contains(uuid))
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
        out.writeUTF(player.getUniqueId().toString());
        out.writeUTF(GsonComponentSerializer.gson().serialize(component));
        player.sendPluginMessage(plugin, Config.MESSAGECHANNEL, out.toByteArray());
    }

    // Start - move these to util

    private boolean isMuted(Player player, String message, String prefix) {
        ChatUser user = ChatUserManager.getChatUser(player.getUniqueId());
        if (user == null) return false;
        if (user.isMuted() || (ChatPlugin.getInstance().serverMuted() && !player.hasPermission("chat.bypass-server-muted"))) {
//        if (Database.get().isPlayerMuted(player.getUniqueId(), null) || (ChatPlugin.getInstance().serverMuted() && !player.hasPermission("chat.bypass-server-muted"))) {
            GalaxyUtility.sendBlockedNotification(prefix, player, message, "");
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
