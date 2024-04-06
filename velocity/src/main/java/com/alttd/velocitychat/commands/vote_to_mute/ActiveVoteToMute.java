package com.alttd.velocitychat.commands.vote_to_mute;

import com.alttd.chat.util.Utility;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

public class ActiveVoteToMute {

    private static final HashMap<String, ActiveVoteToMute> instances = new HashMap<>();
    private static final Component prefix = Utility.parseMiniMessage("<gold>[VoteMute]</gold>");

    private final Instant start;
    private final Player votedPlayer;
    private final Duration duration;
    private HashSet<UUID> votedFor = new HashSet<>();
    private HashSet<UUID> votedAgainst = new HashSet<>();
    private int totalEligibleVoters;

    public static Optional<ActiveVoteToMute> getInstance(String username) {
        if (!instances.containsKey(username))
            return Optional.empty();
        return Optional.of(instances.get(username));
    }

    public ActiveVoteToMute(@NotNull Player votedPlayer, Duration duration, int totalEligibleVoters) {
        this.start = Instant.now();
        this.votedPlayer = votedPlayer;
        this.duration = duration;
        this.totalEligibleVoters = totalEligibleVoters;
        instances.put(votedPlayer.getUsername(), this);
    }

    public void start(@NotNull RegisteredServer registeredServer, Component chatLogs) {
        Component message = Utility.parseMiniMessage(
                String.format("""
                        <prefix> <gold>[VoteMute]</gold> <green>A vote to mute <player> for one hour has been started, please read the logs below before voting.</green>
                        <logs>
                        <prefix> Click: <click:run_command:'/votetomute vote %s yes'><red>Mute</red></click> --- <click:run_command:'/votetomute vote %s no'><yellow>Don't mute</yellow></click>""",
                        votedPlayer.getUsername(), votedPlayer.getUsername()),
                Placeholder.component("prefix", prefix),
                Placeholder.parsed("player", votedPlayer.getUsername()),
                Placeholder.component("logs", chatLogs));
        registeredServer.getPlayersConnected().stream()
                .filter(player -> player.hasPermission("chat.vote-to-mute"))
                .forEach(player -> player.sendMessage(message));
    }

    public void vote(UUID uuid, boolean votedToMute) {
        if (votedToMute) {
            votedFor.add(uuid);
            votedAgainst.remove(uuid);
        } else {
            votedAgainst.add(uuid);
            votedFor.remove(uuid);
        }
    }

    public boolean votePassed() {
        double totalVotes = (votedFor.size() + votedAgainst.size());
        if (totalVotes / totalEligibleVoters  < 0.6) {
            return false;
        }
        return votedFor.size() / totalVotes > 0.6;
    }

    public boolean voteEnded() {
        if (votedFor.size() + votedAgainst.size() == totalEligibleVoters)
            return true;
        return duration.minus(Duration.between(start, Instant.now())).isNegative();
    }
}
