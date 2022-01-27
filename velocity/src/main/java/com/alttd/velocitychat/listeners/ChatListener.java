package com.alttd.velocitychat.listeners;

import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.VelocityChat;
import com.alttd.velocitychat.events.GlobalAdminChatEvent;
import com.alttd.chat.config.Config;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.Template;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
// TODO code CLEANUP
public class ChatListener {

    private VelocityChat plugin;

    public ChatListener() {
        plugin = VelocityChat.getPlugin();
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onGlobalStaffChat(GlobalAdminChatEvent event) {
        String senderName = Config.CONSOLENAME;
        String serverName = "Altitude";
        CommandSource commandSource = event.getSender();
        if (commandSource instanceof Player) {
            Player sender = (Player) event.getSender();
            senderName = sender.getUsername();
            serverName = sender.getCurrentServer().isPresent() ? sender.getCurrentServer().get().getServerInfo().getName() : "Altitude";
        }

        List<Template> templates = new ArrayList<>(List.of(
                Template.template("sender", senderName),
                Template.template("message", event.getMessage()),
                Template.template("server", serverName)
        ));

        Component message = Utility.parseMiniMessage(Config.GACFORMAT, templates);

        plugin.getProxy().getAllPlayers().stream().filter(target -> target.hasPermission("command.chat.globaladminchat")).forEach(target -> {
            target.sendMessage(message);
        });
    }

}
