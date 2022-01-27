package com.alttd.velocitychat.commands;

import com.alttd.velocitychat.VelocityChat;
import com.alttd.chat.config.Config;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;

public class GlobalAdminChat {

    public GlobalAdminChat(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder
                .<CommandSource>literal("globaladminchat")
                .requires(ctx -> ctx.hasPermission("command.chat.globaladminchat"))
                .then(RequiredArgumentBuilder
                        .<CommandSource, String>argument("message",  StringArgumentType.greedyString())
                        .executes(context -> {
                            VelocityChat.getPlugin().getChatHandler().globalAdminChat(context.getSource(), context.getArgument("message", String.class));
                            return 1;
                        })
                )
                .executes(context -> 0)
                .build();

        BrigadierCommand brigadierCommand = new BrigadierCommand(command);

        CommandMeta.Builder metaBuilder = proxyServer.getCommandManager().metaBuilder(brigadierCommand);

        for (String alias : Config.GACECOMMANDALIASES) {
            metaBuilder.aliases(alias);
        }

        CommandMeta meta = metaBuilder.build();

        proxyServer.getCommandManager().register(meta, brigadierCommand);
    }
}
