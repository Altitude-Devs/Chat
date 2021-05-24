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
import net.luckperms.api.LuckPerms;
import net.luckperms.api.node.Node;

public class GlobalChatToggle {

    // todo q - can this be implemented in /gc toggle or /gc true/false
    public GlobalChatToggle(ProxyServer proxyServer) {
        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder
                .<CommandSource>literal("toggleglobalchat")
                .requires(ctx -> ctx instanceof Player)
                .requires(ctx -> ctx.hasPermission("command.proxy.globalchat"))// TODO permission system? load permissions from config?
                .then(RequiredArgumentBuilder
                        .<CommandSource, String>argument("message",  StringArgumentType.greedyString())
                        .executes(context -> {
                            LuckPerms luckPerms = VelocityChat.getPlugin().getLuckPerms();
                            Player player = (Player) context;
                            luckPerms.getUserManager().modifyUser(player.getUniqueId(), user -> {
                                if(player.hasPermission(Config.GCPERMISSION)) { //TODO THIS MUST BE A CONSTANT FROM CONFIG?
                                    user.data().add(Node.builder(Config.GCPERMISSION).build());
                                } else {
                                    user.data().remove(Node.builder(Config.GCPERMISSION).build());
                                }
                            });
                            return 1;
                        })
                )
                .executes(context -> 0)
                .build();

        BrigadierCommand brigadierCommand = new BrigadierCommand(command);

        CommandMeta.Builder metaBuilder = proxyServer.getCommandManager().metaBuilder(brigadierCommand);
        metaBuilder.aliases("togglegc");

        CommandMeta meta = metaBuilder.build();

        proxyServer.getCommandManager().register(meta, brigadierCommand);
    }
}
