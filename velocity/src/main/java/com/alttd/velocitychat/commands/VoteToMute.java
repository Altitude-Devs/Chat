package com.alttd.velocitychat.commands;

import com.alttd.chat.config.Config;
import com.alttd.chat.util.Utility;
import com.alttd.velocitychat.commands.vote_to_mute.ActiveVoteToMute;
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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class VoteToMute {

    public VoteToMute(ProxyServer proxyServer) {
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
                <CommandSource, String>argument("vote", StringArgumentType.string())
                .suggests(((commandContext, suggestionsBuilder) -> {
                    List<String> yesNoValues = Arrays.asList("yes", "no");
                    String remaining = suggestionsBuilder.getRemaining().toLowerCase();
                    yesNoValues.stream()
                            .filter((String str) -> str.toLowerCase().startsWith(remaining))
                            .map(StringArgumentType::escapeIfRequired)
                            .forEach(suggestionsBuilder::suggest);
                    return suggestionsBuilder.buildFuture();
                }));

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
                                                "<red>This player does not have an active vote to mute them</red>"));
                                        return 1;
                                    }
                                    ActiveVoteToMute activeVoteToMute = optionalActiveVoteToMute.get();
                                    String vote = commandContext.getArgument("vote", String.class);
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
                .<CommandSource>literal("votetomute")
                .requires(commandSource -> commandSource.hasPermission("chat.vote-to-mute"))
                .requires(commandSource -> commandSource instanceof Player)
                .then(playerNode
                        .executes(commandContext -> {
                            String playerName = commandContext.getArgument("player", String.class);
                            Optional<Player> optionalPlayer = proxyServer.getPlayer(playerName);
                            if (optionalPlayer.isEmpty()) {
                                commandContext.getSource().sendMessage(Utility.parseMiniMessage(
                                        "<red>Player <player> is not online</red>",
                                        Placeholder.parsed("player", playerName)));
                                return 1;
                            }
                            return 1;
                        }))
                .then(voteNode)
                .executes(context -> {
                    sendHelpMessage(context.getSource());
                    return 1;
                })
                .build();
        //TODO test command
        //TODO add command to pick out the messages
        //TODO add command to go to the next page

        BrigadierCommand brigadierCommand = new BrigadierCommand(command);

        CommandMeta.Builder metaBuilder = proxyServer.getCommandManager().metaBuilder(brigadierCommand);

        CommandMeta meta = metaBuilder.build();

        proxyServer.getCommandManager().register(meta, brigadierCommand);
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
