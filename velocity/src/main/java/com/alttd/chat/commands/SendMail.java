package com.alttd.chat.commands;

import com.alttd.chat.VelocityChat;
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

import java.util.ArrayList;
import java.util.Collection;

public class SendMail {

    public SendMail(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder
                .<CommandSource>literal("mail")
                .requires(ctx -> ctx.hasPermission("command.proxy.mail"))// TODO permission system? load permissions from config?
                .then(LiteralArgumentBuilder.<CommandSource>literal("send")
                        .then(RequiredArgumentBuilder
                                .<CommandSource, String>argument("player", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    Collection<String> possibleValues = new ArrayList<>();
                                    for (Player player : proxyServer.getAllPlayers()) { // todo all chatplayers? this can be heavy
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
                                            // todo construct the mail and notify the player if online?
                                            VelocityChat.getPlugin().getChatHandler().sendMail();
                                            return 1;
                                        })
                                )
                                .executes(context -> {

                                    return 1;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                        // TODO list read, unread, all?
                        .then(LiteralArgumentBuilder.<CommandSource>literal("read")
                                .executes(context -> {

                                    return 1;
                                })
                        )
                        .then(LiteralArgumentBuilder.<CommandSource>literal("unread")
                                .executes(context -> {

                                    return 1;
                                })
                        )
                        .then(LiteralArgumentBuilder.<CommandSource>literal("all")
                                .executes(context -> {

                                    return 1;
                                })
                        )
                        .executes(context -> {

                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("admin")
                        .requires(ctx -> ctx.hasPermission("command.proxy.mail.admin"))// TODO permission system? load permissions from config?
                        // TODO admin command, remove, edit?
                        .executes(context -> {

                            return 1;
                        })
                )
                .executes(context -> {
                    // todo mail help message
                    return 1;
                })
                .build();

        BrigadierCommand brigadierCommand = new BrigadierCommand(command);

        CommandMeta.Builder metaBuilder = proxyServer.getCommandManager().metaBuilder(brigadierCommand);

        /*for (String alias : Config.MAILCOMMANDALIASES) {
            metaBuilder.aliases(alias);
        }*/

        CommandMeta meta = metaBuilder.build();

        proxyServer.getCommandManager().register(meta, brigadierCommand);
    }
}
