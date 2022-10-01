package com.alttd.chat.nicknames;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.database.Queries;
import com.alttd.chat.objects.Nick;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.UUID;

public class NicknamesEvents implements Listener, PluginMessageListener {


    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent e) {

        new BukkitRunnable() {
            @Override
            public void run() {
                if (Nicknames.instance.NickCache.isEmpty()) {
                    Queries.getNicknamesList().forEach(nick -> Nicknames.instance.NickCache.put(nick.getUuid(), nick));
                }

                final Player player = e.getPlayer();
                final Nick nick = Queries.getNick(player.getUniqueId());

                if (nick == null) {
                    Nicknames.getInstance().resetNick(player);
                    return;
                }

                String nickName = nick.getCurrentNick();
                String strippedNick = Nicknames.getInstance().getNick(player);
//                try {
//                    strippedNick = MiniMessage.miniMessage().stripTokens(Nicknames.getInstance().getNick(player));
//                } catch (NullPointerException ignored) {
//                }
//                final String strippedNick = CMIChatColor.stripColor(Nicknames.getInstance().getNick(player));

                //final String cmiNick = Util.CMIChatColor.deColorize(Nicknames.getInstance().getNick(player));

                if (nickName == null) {
                    Nicknames.getInstance().resetNick(player);
                } else if (!nickName.equals(strippedNick)) {
                    Nicknames.getInstance().setNick(player.getUniqueId(), nickName);
                }

                Nicknames.getInstance().NickCache.put(e.getPlayer().getUniqueId(), nick);

                if (player.hasPermission("utility.nick.review")) {
                    int i = 0;
                    for (Nick iNick : Nicknames.getInstance().NickCache.values()) {
                        if (iNick.hasRequest()) {
                            i++;
                        }
                    }

                    if (i > 0) {
                        player.sendMessage(format(Config.NICK_REQUESTS_ON_LOGIN
                                .replace("%amount%", String.valueOf(i))));
                    }
                }
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals(Config.MESSAGECHANNEL)) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (!subChannel.equals("NickNameRequest") && !subChannel.equals("NickNameAccepted")
                && !subChannel.equals("NickNameDenied") && !subChannel.equals("NickNameSet")) {
            return;
        }
        UUID playerUUID;
        OfflinePlayer offlinePlayer;
        String name;
        try {
            short len = in.readShort();
            byte[] msgbytes = new byte[len];
            in.readFully(msgbytes);

            DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));
            playerUUID = UUID.fromString(msgin.readUTF());
            offlinePlayer = ChatPlugin.getInstance().getServer().getOfflinePlayer(playerUUID);
            name = offlinePlayer.getName() == null ? playerUUID.toString() : offlinePlayer.getName();

        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        switch (subChannel) {
            case "NickNameRequest":
                String notification = NickUtilities.applyColor(Config.NICK_REQUEST_NEW
                        .replace("%player%", name));
                TextComponent component = new TextComponent(TextComponent.fromLegacyText(NickUtilities.applyColor(notification)));
                component.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/nick review"));
                component.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(NickUtilities.applyColor("&6Click this text to review the request!")).create()));
                ChatPlugin.getInstance().getServer().getOnlinePlayers().forEach(p -> {
                    if (p.hasPermission("utility.nick.review")) {
                        p.sendMessage(component);
                    }
                });
                Nicknames.getInstance().nickCacheUpdate.add(playerUUID);

                if (offlinePlayer.isOnline()) {
                    Nick nick = Queries.getNick(playerUUID);
                    if (nick != null && nick.getCurrentNick() != null) {
                        Nicknames.getInstance().setNick(offlinePlayer.getUniqueId(), nick.getCurrentNick());
                    }
                }
                break;
            case "NickNameAccepted":
                final String messageAccepted = ChatColor.GREEN + name + "'s nickname was accepted!";
                ChatPlugin.getInstance().getServer().getOnlinePlayers().forEach(p -> {
                    if (p.hasPermission("utility.nick.review")) {
                        p.sendMessage(messageAccepted);
                    }
                });
                //No break on purpose
            case "NickNameSet":
                Nicknames.getInstance().nickCacheUpdate.add(playerUUID);
                if (offlinePlayer.isOnline()) {
                    Nick nick = Queries.getNick(playerUUID);
                    Player target = Bukkit.getPlayer(playerUUID);
                    if (target != null && nick != null && nick.getCurrentNick() != null) {
                        Nicknames.getInstance().setNick(target.getUniqueId(), nick.getCurrentNick());
                        target.sendMessage(format(Config.NICK_CHANGED
                                .replace("%nickname%", nick.getCurrentNick())));
                    }
                }
                break;
            case "NickNameDenied":
                final String messageDenied = ChatColor.RED + name + "'s nickname was denied!";
                Nick nick = Nicknames.getInstance().NickCache.get(playerUUID);

                ChatPlugin.getInstance().getServer().getOnlinePlayers().forEach(p -> {
                    if (p.hasPermission("utility.nick.review")) {
                        p.sendMessage(messageDenied);
                    }
                });

                if (nick.getCurrentNick() == null) {
                    Nicknames.getInstance().NickCache.remove(playerUUID);
                } else {
                    nick.setNewNick(null);
                    nick.setRequestedDate(0);

                    Nicknames.getInstance().NickCache.put(playerUUID, nick);
                }

                if (offlinePlayer.isOnline()) {
                    Player target = Bukkit.getPlayer(playerUUID);

                    if (target == null) break;
                    target.sendMessage(format(Config.NICK_NOT_CHANGED
                            .replace("%nickname%", nick.getCurrentNick())));
                }
                break;
        }
    }

    public static String format(final String m) {
        return NickUtilities.applyColor(m);
    }
}
