package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.api.PrivateMessageEvent;
import com.alttd.chat.config.Config;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

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
                                        proxyServer.getEventManager().fire(new PrivateMessageEvent(context.getSource(), receiver, context.getArgument("message", String.class))).thenAccept((event) -> {
                                            if(event.getResult() == ResultedEvent.GenericResult.allowed()) {
                                                ChatPlugin.getPlugin().getChatHandler().privateMessage(event);
                                            }
                                            // event has finished firing
                                            // do some logic dependent on the result
                                        });
                                        return 1;
                                    } else {
                                        // TODO NOBODY TO REPLY TO
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
