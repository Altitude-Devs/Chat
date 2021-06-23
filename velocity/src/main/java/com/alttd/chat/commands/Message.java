package com.alttd.chat.commands;

import com.alttd.chat.VelocityChat;
import com.alttd.chat.events.PrivateMessageEvent;
import com.alttd.chat.config.Config;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class Message {

    /*public Message(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder
                .<CommandSource>literal("message")
                .requires(ctx -> ctx.hasPermission("command.proxy.message"))// TODO permission system? load permissions from config?
                .then(RequiredArgumentBuilder
                        .<CommandSource, String>argument("player", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            Collection<String> possibleValues = new ArrayList<>();
                            for (Player player : proxyServer.getAllPlayers()) {
                                possibleValues.add(player.getGameProfile().getName());
                            }
                            if(possibleValues.isEmpty()) return Suggestions.empty();
                            String remaining = builder.getRemaining().toLowerCase();
                            for (String str : possibleValues) {
                                if (str.toLowerCase().startsWith(remaining)) {
                                    builder.suggest(str = StringArgumentType.escapeIfRequired(str));
                                }
                            }
                            return builder.buildFuture();
                        })
                        .then(RequiredArgumentBuilder
                                .<CommandSource, String>argument("message",  StringArgumentType.greedyString())
                                .executes(context -> {
                                    Optional<Player> playerOptional = proxyServer.getPlayer(context.getArgument("player", String.class));

                                    if (playerOptional.isPresent()) {
                                        Player receiver = playerOptional.get();
                                        VelocityChat.getPlugin().getChatHandler().privateMessage(context.getSource(), receiver, context.getArgument("message", String.class));
                                        return 1;
                                    } else {
                                        // TODO wrong player message?
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
    }*/

}
