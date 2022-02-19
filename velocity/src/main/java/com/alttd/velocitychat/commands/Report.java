package com.alttd.velocitychat.commands;

import com.alttd.chat.config.Config;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.VelocityChat;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
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
import java.util.Optional;

public class Report {

    public Report(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder
                .<CommandSource>literal("report")
                .requires(ctx -> ctx.hasPermission("command.chat.report"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("username", StringArgumentType.string())
                        .suggests((context, builder) -> {
                            Collection<String> possibleValues = new ArrayList<>();
                            for (Player player : proxyServer.getAllPlayers()) {
                                possibleValues.add(player.getGameProfile().getName());
                            }
                            if(possibleValues.isEmpty()) return Suggestions.empty();
                            String remaining = builder.getRemaining().toLowerCase();
                            for (String str : possibleValues) {
                                if (str.toLowerCase().startsWith(remaining)) {
                                    builder.suggest(StringArgumentType.escapeIfRequired(str));
                                }
                            }
                            return builder.buildFuture();
                        })
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
                                    ServerConnection serverConnection = optionalServerConnection.get();
                                    String serverName = serverConnection.getServer().getServerInfo().getName();
                                    //TODO send message to channel with that server name
                                    return 1;
                                })
                        )
                )
                .executes(context -> 0)
                .build();

        BrigadierCommand brigadierCommand = new BrigadierCommand(command);

        CommandMeta.Builder metaBuilder = proxyServer.getCommandManager().metaBuilder(brigadierCommand);

        CommandMeta meta = metaBuilder.build();

        proxyServer.getCommandManager().register(meta, brigadierCommand);
    }

}
