package com.alttd.chat.handlers;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.api.PrivateMessageEvent;
import com.alttd.chat.config.Config;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

import java.util.*;

public class ChatHandler {

    private List<ChatPlayer> chatPlayers;

    public ChatHandler() {
        chatPlayers = new ArrayList<>();
    }

    public void addPlayer(ChatPlayer chatPlayer) {
        chatPlayers.add(chatPlayer);
    }

    public void removePlayer(ChatPlayer chatPlayer) {
        if(chatPlayer != null)
            chatPlayers.remove(chatPlayer);
    }

    public void removePlayer(UUID uuid) {
        removePlayer(getChatPlayer(uuid));
    }

    public ChatPlayer getChatPlayer(UUID uuid) {
        for(ChatPlayer p: chatPlayers) {
            if(p.getUuid() == uuid)
                return p;
        }
        return null;
    }

    public List<ChatPlayer> getChatPlayers() {
        return Collections.unmodifiableList(chatPlayers);
    }

    public void privateMessage(PrivateMessageEvent event) {
        String senderName;
        String receiverName;
        CommandSource commandSource = event.getSender();
        if (commandSource instanceof Player) {
            Player sender = (Player) event.getSender();
            senderName = sender.getUsername();
            //plugin.getChatHandler().getChatPlayer(sender.getUniqueId()).setReplyTarget(event.getRecipient().getUniqueId()); // TODO this needs to be cleaner
        } else {
            senderName = "Console"; // TODO console name from config
        }
        receiverName = event.getRecipient().getUsername();

        MiniMessage miniMessage = MiniMessage.get();

        Map<String, String> map = new HashMap<>();

        map.put("sender", senderName);
        map.put("receiver", receiverName);
        map.put("message", event.getMessage());
        map.put("server", event.getRecipient().getCurrentServer().isPresent() ? event.getRecipient().getCurrentServer().get().getServerInfo().getName() : "Altitude");

        Component senderMessage = miniMessage.parse(Config.MESSAGESENDER, map);
        Component receiverMessage = miniMessage.parse(Config.MESSAGERECIEVER, map);

        event.getSender().sendMessage(senderMessage);
        event.getRecipient().sendMessage(receiverMessage);
    }

    public void globalChat(CommandSource source, String message) {
        String senderName, serverName, prefix;
        Map<String, String> map = new HashMap<>();

        if (source instanceof Player) {
            Player sender = (Player) source;
            senderName = sender.getUsername();
            serverName = sender.getCurrentServer().isPresent() ? sender.getCurrentServer().get().getServerInfo().getName() : "Altitude";
            prefix = getPrefix(sender);
        } else {
            senderName = "Console"; // TODO console name from config
            serverName = "Altitude";
            prefix = "";
        }

        MiniMessage miniMessage = MiniMessage.get();

        map.put("sender", senderName);
        map.put("message", message);
        map.put("server", serverName);
        map.put("prefix", prefix);

        Component component = miniMessage.parse(Config.GCFORMAT, map);

        for(Player p: ChatPlugin.getPlugin().getProxy().getAllPlayers()) {
            if(p.hasPermission(Config.GCPERMISSION));
                p.sendMessage(component);
                //TODO send global chat with format from config
        }

    }

    /**
     * returns a component containing all prefixes a player has.
     *
     * @param player the player
     * @return a prefix component
     */
    public String getPrefix(Player player) {
        return getPrefix(player, false);
    }

    /**
     * returns a component containing all or only the highest prefix a player has.
     *
     * @param player the player
     * @param highest
     * @return a prefix component
     */
    public String getPrefix(Player player, boolean highest) {
        // TODO cache these components on load, and return them here?
        StringBuilder prefix = new StringBuilder();
        LuckPerms luckPerms = ChatPlugin.getPlugin().API().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(player.getUniqueId());
        if(user == null) return "";
        if(!highest) {
            Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());
            inheritedGroups.stream()
                    .sorted(Comparator.comparingInt(o -> o.getWeight().orElse(0)))
                    .forEach(group -> {
                        if (Config.PREFIXGROUPS.contains(group.getName())) {
                            prefix.append("<white>[").append(group.getCachedData().getMetaData().getPrefix()).append("]</white>");
                        }
                    });
        }
        LegacyComponentSerializer.builder().character('&').hexColors();
        prefix.append("<white>[").append(user.getCachedData().getMetaData().getPrefix()).append("]</white>");
        /*component= MiniMessage.get().parse(prefix.toString());
        CompletableFuture<User> userFuture = luckPerms.getUserManager().loadUser(player.getUniqueId());
        userFuture.thenAcceptAsync(user -> {
            Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());
            inheritedGroups.stream()
                    .sorted((o1, o2) -> Integer.compare(o1.getWeight().orElse(0), o2.getWeight().orElse(0)))
                    .forEach(group -> {
                        if(Config.PREFIXGROUPS.contains(group.getName())) {
                            prefix.append("<white>[").append(group.getCachedData().getMetaData().getPrefix()).append("]</white>");
                        }
                    });
             return MiniMessage.get().parse(prefix.toString());
        });*/
        //return MiniMessage.get().parse(prefix.toString());
        return prefix.toString();
    }
}
