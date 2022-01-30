package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.Party;
import com.alttd.chat.util.Utility;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.apache.commons.lang.StringUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class PartyChat implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) { // must be a player
            return true;
        }

        if(args.length == 0) {
            // TODO: 08/08/2021 lock into party chat
            return true;
        }

        String message = StringUtils.join(args, " ", 0, args.length);

        new BukkitRunnable() {
            @Override
            public void run() {
                ChatPlugin.getInstance().getChatHandler().partyMessage(player, message); //TODO send over proxy
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());

        return true;
    }
}
