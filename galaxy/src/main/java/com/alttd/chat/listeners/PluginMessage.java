package com.alttd.chat.listeners;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.util.ALogger;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import java.util.UUID;

public class PluginMessage implements PluginMessageListener {

    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] bytes) {
        if (!channel.equals(Config.MESSAGECHANNEL)) {
            return;
        }
        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String subChannel = in.readUTF();
        UUID uuid;String target; Player p;
        switch (subChannel) {
            case "privatemessagesend":
                uuid = UUID.fromString(in.readUTF());
                target = in.readUTF();
                p = Bukkit.getPlayer(uuid);
                if(p != null) {
                    ChatUser user = ChatUserManager.getChatUser(uuid);
                    user.setReplyTarget(target);
                    p.sendMessage(GsonComponentSerializer.gson().deserialize(in.readUTF()));
                    Component spymessage = GsonComponentSerializer.gson().deserialize(in.readUTF());
                    for(Player pl : Bukkit.getOnlinePlayers()) {
                        if(pl.hasPermission("chat.social-spy")) { // todo add a toggle for social spy
                            pl.sendMessage(spymessage);
                        }
                    }
                }
                break;
            case "privatemessagesreceived":
                uuid = UUID.fromString(in.readUTF());
                target = in.readUTF();
                p = Bukkit.getPlayer(uuid);
                if(p != null) {
                    ChatUser user = ChatUserManager.getChatUser(uuid);
                    user.setReplyTarget(target);
                    p.sendMessage(GsonComponentSerializer.gson().deserialize(in.readUTF()));
                }
                break;
            case "globalchat":
                break;
            default:
                break;
        }
    }

}
/*    // todo implement AdvancedChatFilter for this like this
    //      send pluginmessage to backend server and return a shatteredcomponent to be reparsed by proxy
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
}*/
