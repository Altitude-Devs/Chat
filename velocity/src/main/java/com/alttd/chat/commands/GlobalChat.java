package com.alttd.chat.commands;

import com.alttd.chat.VelocityChat;
import com.alttd.chat.config.Config;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;

public class GlobalChat {

    public GlobalChat(ProxyServer proxyServer) {
//        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder
//                .<CommandSource>literal("globalchat")
//                .requires(ctx -> ctx.hasPermission("command.proxy.globalchat"))// TODO permission system? load permissions from config?
//                .requires(ctx -> ctx instanceof Player) // players only can use this
//                .then(RequiredArgumentBuilder
//                        .<CommandSource, String>argument("message",  StringArgumentType.greedyString())
//                        .executes(context -> {
//                            VelocityChat.getPlugin().getChatHandler().globalChat((Player) context.getSource(), context.getArgument("message", String.class));
//                            return 1;
//                        })
//                )
//                .executes(context -> 0) // todo info message /usage
//                .build();
//
//        BrigadierCommand brigadierCommand = new BrigadierCommand(command);
//
//        CommandMeta.Builder metaBuilder = proxyServer.getCommandManager().metaBuilder(brigadierCommand);
//
//        for (String alias : Config.GCALIAS) {
//            metaBuilder.aliases(alias);
//        }
//        CommandMeta meta = metaBuilder.build();
//
//        proxyServer.getCommandManager().register(meta, brigadierCommand);
    }
}
