package com.alttd.chat.commands;

import com.alttd.chat.api.MessageEvent;
import com.alttd.chat.config.Config;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Message {

    public Message(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder
                .<CommandSource>literal("message")
                .requires(ctx -> ctx.hasPermission("command.proxy.message"))// TODO permission system? load permissions from config?
                .then(RequiredArgumentBuilder
                        .<CommandSource, String>argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            for (Player player : proxyServer.getAllPlayers()) {
                                builder.suggest(player.getGameProfile().getName());
                            }
                            return builder.buildFuture();
                        })
                        .then(RequiredArgumentBuilder
                                .<CommandSource, String>argument("message",  StringArgumentType.greedyString())
                                .executes(context -> {
                                    Optional<Player> playerOptional = proxyServer.getPlayer(context.getArgument("player", String.class));

                                    if (playerOptional.isPresent()) {
                                        Player receiver = playerOptional.get();
                                        proxyServer.getEventManager().fire(new MessageEvent(context.getSource(), receiver, context.getArgument("message", String.class)));

                                        return 1;
                                    }
                                    return 0;
                                })
                        )
                        .executes(context -> 0)
                )
                .executes(context -> 0)
                .build();

        BrigadierCommand brigadierCommand = new BrigadierCommand(command);

        CommandMeta.Builder metaBuilder = proxyServer.getCommandManager().metaBuilder(brigadierCommand);

        for (String alias : Config.MESSAGECOMMANDALIASES) {
            metaBuilder.aliases(alias);
        }

        CommandMeta meta = metaBuilder.build();

        proxyServer.getCommandManager().register(meta, brigadierCommand);
    }

}
