package com.alttd.chat.commands;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

public class GlobalChat {

    // todo move to server implementation and send plugin event to allowed servers, this means we can implement [i] in here
    public GlobalChat(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder
                .<CommandSource>literal("globalchat")
                .requires(ctx -> ctx.hasPermission(Config.GCPERMISSION))
                .requires(ctx -> ctx.hasPermission("command.proxy.globalchat"))// TODO permission system? load permissions from config?
                .then(RequiredArgumentBuilder
                        .<CommandSource, String>argument("message",  StringArgumentType.greedyString())
                        .executes(context -> {
                            ChatPlugin.getPlugin().getChatHandler().globalChat(context.getSource(), context.getArgument("message", String.class));
                            return 1;
                        })
                )
                .executes(context -> 0)
                .build();

        BrigadierCommand brigadierCommand = new BrigadierCommand(command);

        CommandMeta.Builder metaBuilder = proxyServer.getCommandManager().metaBuilder(brigadierCommand);

        for (String alias : Config.GCCOMMANDALIASES) {
            metaBuilder.aliases(alias);
        }

        CommandMeta meta = metaBuilder.build();

        proxyServer.getCommandManager().register(meta, brigadierCommand);
    }
}
