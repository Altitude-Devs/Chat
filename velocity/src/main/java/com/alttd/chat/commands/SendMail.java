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

        RequiredArgumentBuilder<CommandSource, String> playerNode = RequiredArgumentBuilder
                .<CommandSource, String>argument("player", StringArgumentType.string())
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
                .executes(context -> {
                    sendHelpMessage(context.getSource());
                    return 1;
                });

        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder
                .<CommandSource>literal("mail")
                .requires(ctx -> ctx.hasPermission("command.proxy.mail"))
                .then(LiteralArgumentBuilder.<CommandSource>literal("send")
                        .then(playerNode
                                .then(RequiredArgumentBuilder
                                        .<CommandSource, String>argument("message",  StringArgumentType.greedyString())
                                        .executes(context -> {
                                            VelocityChat.getPlugin().getChatHandler().sendMail(context.getSource(), context.getArgument("player", String.class), context.getArgument("message", String.class));
                                            return 1;
                                        })
                                )
                                .executes(context -> {
                                    sendHelpMessage(context.getSource());
                                    return 1;
                                })
                        )
                        .executes(context -> {
                            sendHelpMessage(context.getSource());
                            return 1;
                        })
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("list")
                        .requires(ctx -> ctx.hasPermission("command.proxy.list"))// TODO permission
                        .then(LiteralArgumentBuilder.<CommandSource>literal("unread")
                                .executes(context -> {
                                    VelocityChat.getPlugin().getChatHandler().readMail(context.getSource(), true);
                                    return 1;
                                })
                        )
                        .then(LiteralArgumentBuilder.<CommandSource>literal("all")
                                .executes(context -> {
                                    VelocityChat.getPlugin().getChatHandler().readMail(context.getSource(), false);
                                    return 1;
                                })
                        )
                        .then(playerNode
                                .then(LiteralArgumentBuilder.<CommandSource>literal("unread")
                                        .executes(context -> {
                                            VelocityChat.getPlugin().getChatHandler().readMail(context.getSource(), context.getArgument("player", String.class), true);
                                            return 1;
                                        })
                                )
                                .then(LiteralArgumentBuilder.<CommandSource>literal("all")
                                        .executes(context -> {
                                            VelocityChat.getPlugin().getChatHandler().readMail(context.getSource(), context.getArgument("player", String.class), false);
                                            return 1;
                                        })
                                )
                                .executes(context -> {
                                    sendHelpMessage(context.getSource());
                                    return 1;
                                })
                        )
                )
                .then(LiteralArgumentBuilder.<CommandSource>literal("admin")
                        .requires(ctx -> ctx.hasPermission("command.proxy.mail.admin"))// TODO permission
                        // TODO admin command, remove, edit?
                        .executes(context -> {
                            sendAdminHelpMessage(context.getSource());
                            return 1;
                        })
                )
                .executes(context -> {
                    sendHelpMessage(context.getSource());
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

    private void sendHelpMessage(CommandSource commandSource) {

    }

    private void sendAdminHelpMessage(CommandSource commandSource) {

    }
}
