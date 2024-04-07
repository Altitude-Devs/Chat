package com.alttd.velocitychat.commands;

import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.commands.vote_to_mute.ActiveVoteToMute;
import com.alttd.velocitychat.commands.vote_to_mute.VoteToMuteStarter;
import com.mojang.brigadier.arguments.IntegerArgumentType;
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
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VoteToMuteHelper {

    public VoteToMuteHelper(ProxyServer proxyServer) {
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

        RequiredArgumentBuilder<CommandSource, String> yesNoNode = RequiredArgumentBuilder.
                <CommandSource, String>argument("yesNo", StringArgumentType.string())
                .suggests(((commandContext, suggestionsBuilder) -> {
                    List<String> yesNoValues = Arrays.asList("yes", "no");
                    String remaining = suggestionsBuilder.getRemaining().toLowerCase();
                    yesNoValues.stream()
                            .filter((String str) -> str.toLowerCase().startsWith(remaining))
                            .map(StringArgumentType::escapeIfRequired)
                            .forEach(suggestionsBuilder::suggest);
                    return suggestionsBuilder.buildFuture();
                }));

        LiteralArgumentBuilder<CommandSource> pageNode = LiteralArgumentBuilder
                .<CommandSource>literal("page")
                .requires(commandSource -> commandSource.hasPermission("chat.vote-to-mute"))
                .then(RequiredArgumentBuilder.<CommandSource, Integer>argument("page number", IntegerArgumentType.integer(1))
                        .suggests(((commandContext, suggestionsBuilder) -> {
                            if (!(commandContext.getSource() instanceof Player player)) {
                                return suggestionsBuilder.buildFuture();
                            }
                            Optional<VoteToMuteStarter> instance = VoteToMuteStarter.getInstance(player.getUniqueId());
                            if (instance.isEmpty()) {
                                return suggestionsBuilder.buildFuture();
                            }
                            VoteToMuteStarter voteToMuteStarter = instance.get();
                            String remaining = suggestionsBuilder.getRemaining().toLowerCase();
                            int totalPages = voteToMuteStarter.getTotalPages();
                            IntStream.range(1, totalPages + 1)
                                    .mapToObj(String::valueOf)
                                    .filter((String str) -> str.toLowerCase().startsWith(remaining))
                                    .map(StringArgumentType::escapeIfRequired)
                                    .forEach(suggestionsBuilder::suggest);
                            return suggestionsBuilder.buildFuture();
                        }))
                        .executes(commandContext -> {
                            if (!(commandContext.getSource() instanceof Player player)) {
                                commandContext.getSource().sendMessage(Utility.parseMiniMessage("<red>Only players can use this command.</red>"));
                                return 1;
                            }
                            Optional<VoteToMuteStarter> instance = VoteToMuteStarter.getInstance(player.getUniqueId());
                            if (instance.isEmpty()) {
                                commandContext.getSource().sendMessage(Utility.parseMiniMessage("<red>You don't have an active vote to mute.</red>"));
                                return 1;
                            }
                            int pageNumber = commandContext.getArgument("page number", Integer.class);
                            instance.get().showPage(pageNumber);
                            return 1;
                        })
                ).executes(commandContext -> {
                    sendHelpMessage(commandContext.getSource());
                    return 1;
                });

        LiteralArgumentBuilder<CommandSource> enterPageNode = LiteralArgumentBuilder
                .<CommandSource>literal("messages")
                .requires(commandSource -> commandSource.hasPermission("chat.vote-to-mute"))
                .then(RequiredArgumentBuilder.<CommandSource, String>argument("list of messages", StringArgumentType.greedyString())
                        .executes(commandContext -> {
                            if (!(commandContext.getSource() instanceof Player player)) {
                                commandContext.getSource().sendMessage(Utility.parseMiniMessage("<red>Only players can use this command.</red>"));
                                return 1;
                            }
                            Optional<VoteToMuteStarter> instance = VoteToMuteStarter.getInstance(player.getUniqueId());
                            if (instance.isEmpty()) {
                                commandContext.getSource().sendMessage(Utility.parseMiniMessage("<red>You don't have an active vote to mute.</red>"));
                                return 1;
                            }
                            String listOfPages = commandContext.getArgument("list of messages", String.class);
                            if (!listOfPages.matches("([1-9][0-9]*, )*[1-9][0-9]*")) {
                                commandContext.getSource().sendMessage(Utility.parseMiniMessage("<red>Please make sure to format the command correctly.</red>"));
                                return 1;
                            }
                            VoteToMuteStarter voteToMuteStarter = instance.get();

                            List<Integer> collect = Arrays.stream(listOfPages.split(", "))
                                    .map(Integer::parseInt)
                                    .collect(Collectors.toList());
                            Optional<Integer> max = collect.stream().max(Integer::compare);
                            if (max.isEmpty()) {
                                commandContext.getSource().sendMessage(Utility.parseMiniMessage("<red>Some of your selected messages do not exist.</red>"));
                                return 1;
                            }
                            int highestLogEntry = max.get();

                            if (voteToMuteStarter.getTotalLogEntries() > highestLogEntry) {
                                commandContext.getSource().sendMessage(Utility.parseMiniMessage("<red>Some of your selected messages do not exist.</red>"));
                                return 1;
                            }

                            Optional<ServerConnection> currentServer = player.getCurrentServer();
                            if (currentServer.isEmpty()) {
                                sendHelpMessage(commandContext.getSource());
                                return 1;
                            }

                            Component chatLogs = voteToMuteStarter.getChatLogsAndClose(collect);
                            RegisteredServer server = currentServer.get().getServer();
                            long count = getTotalEligiblePlayers(server, voteToMuteStarter.countLowerRanks());
                            new ActiveVoteToMute(voteToMuteStarter.getVotedPlayer(), server, proxyServer, Duration.ofMinutes(5),
                                    (int) count, voteToMuteStarter.countLowerRanks(), chatLogs)
                                    .start();
                            return 1;
                        })
                ).executes(commandContext -> {
                    sendHelpMessage(commandContext.getSource());
                    return 1;
                });

        LiteralArgumentBuilder<CommandSource> voteNode = LiteralArgumentBuilder
                .<CommandSource>literal("vote")
                .then(playerNode
                        .then(yesNoNode
                                .executes(commandContext -> {
                                    if (!(commandContext.getSource() instanceof Player)) {
                                        commandContext.getSource().sendMessage(Utility.parseMiniMessage(
                                                "<red>Only players are allowed to vote</red>"));
                                    }
                                    Player source = (Player) commandContext.getSource();
                                    String playerName = commandContext.getArgument("player", String.class);
                                    Optional<ActiveVoteToMute> optionalActiveVoteToMute = ActiveVoteToMute.getInstance(playerName);
                                    if (optionalActiveVoteToMute.isEmpty()) {
                                        commandContext.getSource().sendMessage(Utility.parseMiniMessage(
                                                "<red>This player does not have an active vote to mute them.</red>"));
                                        return 1;
                                    }
                                    ActiveVoteToMute activeVoteToMute = optionalActiveVoteToMute.get();

                                    if (!activeVoteToMute.countLowerRanks()) {
                                        if (!source.hasPermission("chat.vote-to-mute")) {
                                            source.sendMessage(Utility.parseMiniMessage("<red>You are not eligible to vote.</red>"));
                                            return 1;
                                        }
                                    }

                                    String vote = commandContext.getArgument("yesno", String.class);
                                    switch (vote.toLowerCase()) {
                                        case "yes" -> activeVoteToMute.vote(source.getUniqueId(), true);
                                        case "no" -> activeVoteToMute.vote(source.getUniqueId(), false);
                                        default -> commandContext.getSource().sendMessage(Utility.parseMiniMessage(
                                                "<red><vote> is not a valid vote option</red>", Placeholder.parsed("vote", vote)));
                                    }
                                    return 1;
                                })).executes(context -> {
                            sendHelpMessage(context.getSource());
                            return 1;
                        })).executes(context -> {
                    sendHelpMessage(context.getSource());
                    return 1;
                });

        LiteralCommandNode<CommandSource> command = LiteralArgumentBuilder
                .<CommandSource>literal("votetomutehelper")
                .requires(commandSource -> commandSource.hasPermission("chat.backup-vote-to-mute"))
                .requires(commandSource -> commandSource instanceof Player)
                .then(voteNode)
                .then(pageNode)
                .then(enterPageNode)
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
        commandSource.sendMessage(Utility.parseMiniMessage("<red>Use: <gold>/votetomutehelper <player></gold>.</red>"));
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
