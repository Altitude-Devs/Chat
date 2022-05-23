package com.alttd.velocitychat.commands;

import com.alttd.chat.config.Config;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.listeners.ProxyPlayerListener;
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
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class SilentJoinCommand {

    public SilentJoinCommand(ProxyServer proxyServer) {

        RequiredArgumentBuilder<CommandSource, String> serverNode = RequiredArgumentBuilder
                .<CommandSource, String>argument("server", StringArgumentType.string())
                .suggests((context, builder) -> {
                    Collection<String> possibleValues = new ArrayList<>();
                    for (RegisteredServer server : proxyServer.getAllServers()) {
                        possibleValues.add(server.getServerInfo().getName());
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
                .<CommandSource>literal("silentjoin")
                .requires(ctx -> ctx.hasPermission("command.chat.silent-join"))
                .then(serverNode
                        .executes(context -> {
                            if (!(context.getSource() instanceof Player player)) {
                                context.getSource().sendMessage(Utility.parseMiniMessage(Config.NO_CONSOLE));
                                return 1;
                            }
                            if (player.getCurrentServer().isEmpty()) {
                                return 1;
                            }

                            String server = context.getArgument("server", String.class);
                            Optional<RegisteredServer> optionalServer = proxyServer.getServer(server);
                            if (optionalServer.isEmpty()) {
                                player.sendMessage(Utility.parseMiniMessage(Config.SILENT_JOIN_NO_SERVER));
                                return 1;
                            }
                            RegisteredServer registeredServer = optionalServer.get();
                            player.sendMessage(Utility.parseMiniMessage(Config.SILENT_JOIN_JOINING,
                                    Placeholder.unparsed("server", registeredServer.getServerInfo().getName())));
                            ProxyPlayerListener.addSilentJoin(player.getUniqueId());
                            player.createConnectionRequest(registeredServer);
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

        for (String alias : Config.SILENT_JOIN_COMMAND_ALIASES) {
            metaBuilder.aliases(alias);
        }

        CommandMeta meta = metaBuilder.build();

        proxyServer.getCommandManager().register(meta, brigadierCommand);
    }

    private void sendHelpMessage(CommandSource commandSource) {

    }

    private void sendAdminHelpMessage(CommandSource commandSource) {

    }
}
