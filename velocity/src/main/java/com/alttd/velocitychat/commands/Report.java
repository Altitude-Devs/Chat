package com.alttd.velocitychat.commands;

import com.alttd.chat.config.Config;
import com.alttd.chat.util.Utility;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;

import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.lib.net.dv8tion.jda.api.EmbedBuilder;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.minimessage.MiniMessage;


import java.util.ArrayList;
import java.util.Collection;

import java.awt.*;

import java.util.Optional;

public class Report {

    public Report(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder
                .<CommandSource>literal("report")
                .requires(ctx -> ctx.hasPermission("command.chat.report"))
                .then(RequiredArgumentBuilder
                        .<CommandSource, String>argument("report",  StringArgumentType.greedyString())
                        .executes(context -> {
                            if (!(context.getSource() instanceof Player player)) {
                                context.getSource().sendMessage(Utility.parseMiniMessage(Config.NO_CONSOLE));
                                return 1;
                            }
                            Optional<ServerConnection> optionalServerConnection = player.getCurrentServer();
                            if (optionalServerConnection.isEmpty()) {
                                return 1;
                            }
                            String report = context.getArgument("report", String.class);
                            if (report.split(" ").length < 3) {
                                player.sendMessage(Utility.parseMiniMessage(Config.REPORT_TOO_SHORT));
                                return 1;
                            }
                            ServerConnection serverConnection = optionalServerConnection.get();
                            String serverName = serverConnection.getServer().getServerInfo().getName();

                            EmbedBuilder embedBuilder = new EmbedBuilder();
                            embedBuilder.setAuthor(player.getUsername(), null, "https://crafatar.com/avatars/" + player.getUniqueId() + "?overlay");
                            embedBuilder.setTitle("Player Report");
                            embedBuilder.setColor(Color.CYAN);
                            embedBuilder.addField("Incident",
                                    report,
                                    false);
                            embedBuilder.addField("Server",
                                    serverName.substring(0, 1).toUpperCase() + serverName.substring(1),
                                    false);

                            Long id = Config.serverChannelId.get(serverName.toLowerCase());
                            if (id <= 0)
                                id = Config.serverChannelId.get("general");
                            DiscordLink.getPlugin().getBot().sendEmbedToDiscord(id, embedBuilder, -1);
                            player.sendMessage(Utility.parseMiniMessage(Config.REPORT_SENT));
                            return 1;
                        })
                )
                .executes(context -> {
                    context.getSource().sendMessage(Utility.parseMiniMessage(Config.HELP_REPORT));
                    return 0;
                })
                .build();

        BrigadierCommand brigadierCommand = new BrigadierCommand(command);

        CommandMeta.Builder metaBuilder = proxyServer.getCommandManager().metaBuilder(brigadierCommand);

        CommandMeta meta = metaBuilder.build();

        proxyServer.getCommandManager().register(meta, brigadierCommand);
    }

}
