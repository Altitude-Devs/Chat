package com.alttd.velocitychat.commands;

import com.alttd.chat.objects.chat_log.ChatLogHandler;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.commands.vote_to_mute.VoteToMuteStarter;
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
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VoteToMute {

    public VoteToMute(ProxyServer proxyServer, ChatLogHandler chatLogHandler) {
        RequiredArgumentBuilder<CommandSource, String> playerNode = RequiredArgumentBuilder
                .<CommandSource, String>argument("player", StringArgumentType.string())
                .suggests((context, builder) -> {
                    List<Player> possiblePlayers;
                    if (context.getSource() instanceof Player player) {
                        Optional<ServerConnection> currentServer = player.getCurrentServer();
                        if (currentServer.isPresent()) {
                            possiblePlayers = getEligiblePlayers(currentServer.get().getServer());
                        } else {
                            possiblePlayers = getEligiblePlayers(proxyServer);
                        }
                    } else {
                        possiblePlayers = getEligiblePlayers(proxyServer);
                    }
                    Collection<String> possibleValues = possiblePlayers.stream()
                            .map(Player::getUsername)
                            .toList();

                    if (possibleValues.isEmpty())
                        return Suggestions.empty();

                    String remaining = builder.getRemaining().toLowerCase();
                    possibleValues.stream()
                            .filter(str -> str.toLowerCase().startsWith(remaining))
                            .map(StringArgumentType::escapeIfRequired)
                            .forEach(builder::suggest);
                    return builder.buildFuture();
                })
                .executes(context -> {
                    sendHelpMessage(context.getSource());
                    return 1;
                });

        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder
                .<CommandSource>literal("votetomute")
                .requires(commandSource -> commandSource.hasPermission("chat.vote-to-mute"))
                .requires(commandSource -> commandSource instanceof Player)
                .then(playerNode
                        .suggests(((commandContext, suggestionsBuilder) -> {
                            if (!(commandContext.getSource() instanceof Player player)) {
                                return suggestionsBuilder.buildFuture();
                            }
                            Optional<ServerConnection> currentServer = player.getCurrentServer();
                            if (currentServer.isEmpty()) {
                                sendHelpMessage(commandContext.getSource());
                                return suggestionsBuilder.buildFuture();
                            }
                            String remaining = suggestionsBuilder.getRemaining().toLowerCase();
                            currentServer.get().getServer().getPlayersConnected().stream()
                                    .filter(connectedPlayer -> connectedPlayer.hasPermission("chat.affected-by-vote-to-mute"))
                                    .map(Player::getUsername)
                                    .filter((String str) -> str.toLowerCase().startsWith(remaining))
                                    .map(StringArgumentType::escapeIfRequired)
                                    .forEach(suggestionsBuilder::suggest);
                            return suggestionsBuilder.buildFuture();
                        }))
                        .executes(commandContext -> {
                            String playerName = commandContext.getArgument("player", String.class);
                            Optional<Player> optionalPlayer = proxyServer.getPlayer(playerName);
                            if (optionalPlayer.isEmpty()) {
                                commandContext.getSource().sendMessage(Utility.parseMiniMessage(
                                        "<red>Player <player> is not online.</red>",
                                        Placeholder.parsed("player", playerName)));
                                return 1;
                            }
                            Player voteTarget = optionalPlayer.get();
                            if (!voteTarget.hasPermission("chat.affected-by-vote-to-mute")) {
                                commandContext.getSource().sendMessage(Utility.parseMiniMessage(
                                        "<red>Player <player> can not be muted by a vote.</red>",
                                        Placeholder.parsed("player", playerName)));
                                return 1;
                            }
                            Player player = (Player) commandContext.getSource();
                            Optional<ServerConnection> currentServer = player.getCurrentServer();
                            if (currentServer.isEmpty()) {
                                sendHelpMessage(commandContext.getSource());
                                return 1;
                            }
                            RegisteredServer server = currentServer.get().getServer();
                            if (currentServer.get().getServer().getPlayersConnected().stream().anyMatch(onlinePlayer -> onlinePlayer.hasPermission("chat.staff"))) {
                                commandContext.getSource().sendMessage(Utility.parseMiniMessage("<red>There is a staff member online, so vote to mute can not be used. Please contact a staff member for help instead.</red>"));
                                return 1;
                            }
                            boolean countLowerRanks = false;
                            long count = getTotalEligiblePlayers(server, false);
                            if (count < 6) {
                                countLowerRanks = true;
                                count = getTotalEligiblePlayers(server, true);
                                if (count < 6) {
                                    commandContext.getSource().sendMessage(Utility.parseMiniMessage("<red>Not enough eligible players online to vote.</red>"));
                                    return 1;
                                }
                            }
                            new VoteToMuteStarter(chatLogHandler, voteTarget, player, server.getServerInfo().getName(), countLowerRanks)
                                    .start();
                            return 1;
                        }))
                .executes(context -> {
                    sendHelpMessage(context.getSource());
                    return 1;
                })
                .build();

        BrigadierCommand brigadierCommand = new BrigadierCommand(command);

        CommandMeta.Builder metaBuilder = proxyServer.getCommandManager().metaBuilder(brigadierCommand);

        CommandMeta meta = metaBuilder.build();

        proxyServer.getCommandManager().register(meta, brigadierCommand);
    }

    private int getTotalEligiblePlayers(RegisteredServer server, boolean countLowerRanks) {
        return (int) server.getPlayersConnected().stream()
                .filter(player -> countLowerRanks ? player.hasPermission("chat.backup-vote-to-mute") : player.hasPermission("chat.vote-to-mute"))
                .count();
    }

    private void sendHelpMessage(CommandSource commandSource) {
        commandSource.sendMessage(Utility.parseMiniMessage("<red>Use: <gold>/votetomute <player></gold>.</red>"));
    }

    private List<Player> getEligiblePlayers(ProxyServer proxyServer) {
        return proxyServer.getAllPlayers().stream()
                .filter(player -> player.hasPermission("chat.affected-by-vote-to-mute"))
                .collect(Collectors.toList());
    }

    private List<Player> getEligiblePlayers(RegisteredServer registeredServer) {
        return registeredServer.getPlayersConnected().stream()
                .filter(player -> player.hasPermission("chat.affected-by-vote-to-mute"))
                .collect(Collectors.toList());

    }

}
