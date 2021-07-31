package com.alttd.chat.listeners;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.util.ALogger;
import com.alttd.chat.util.Utility;
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
        switch (subChannel) {
            case "privatemessage": {
                UUID uuid = UUID.fromString(in.readUTF());
                String target = in.readUTF();
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) {
                    p.sendMessage(GsonComponentSerializer.gson().deserialize(in.readUTF()));
                    ChatUser user = ChatUserManager.getChatUser(uuid);
                    user.setReplyTarget(target);
                }
                break;
            }
            case "globalchat": {
                if (ChatPlugin.getInstance().serverGlobalChatEnabled() && !ChatPlugin.getInstance().serverMuted()) {
                    String uuidString = in.readUTF();
                    if (!uuidString.matches("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b")) {
                        Bukkit.broadcast(GsonComponentSerializer.gson().deserialize(uuidString), Config.GCPERMISSION);
                        break;
                    }

                    UUID uuid = UUID.fromString(uuidString);
                    String message = in.readUTF();

                    Bukkit.getOnlinePlayers().stream().filter(p -> p.hasPermission(Config.GCPERMISSION)).forEach(p -> {
                        ChatUser chatUser = ChatUserManager.getChatUser(p.getUniqueId());
                        if (chatUser.getIgnoredPlayers().contains(uuid)) {
                            p.sendMessage(GsonComponentSerializer.gson().deserialize(message));
                        }
                    });
                }
                break;
            }
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
