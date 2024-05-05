package com.alttd.velocitychat.commands.vote_to_mute;

import com.alttd.chat.config.Config;
import com.alttd.chat.util.ALogger;
import com.alttd.chat.util.Utility;
import com.alttd.proxydiscordlink.DiscordLink;
import com.alttd.proxydiscordlink.lib.net.dv8tion.jda.api.EmbedBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class ActiveVoteToMute {

    private static final HashMap<String, ActiveVoteToMute> instances = new HashMap<>();
    private static final Component prefix = Utility.parseMiniMessage("<gold>[VoteMute]</gold>");

    private final Player votedPlayer;
    private HashSet<UUID> votedFor = new HashSet<>();
    private HashSet<UUID> votedAgainst = new HashSet<>();
    private int totalEligibleVoters;
    private final boolean countLowerRanks;
    private final RegisteredServer server;
    private final ProxyServer proxyServer;
    private final Component chatLogs;
    private boolean endedVote = false;

    public static Optional<ActiveVoteToMute> getInstance(String username) {
        if (!instances.containsKey(username))
            return Optional.empty();
        return Optional.of(instances.get(username));
    }

    public static void removePotentialVoter(Player player, RegisteredServer previousServer) {
        if (!player.hasPermission("chat.backup-vote-to-mute"))
            return;
        if (player.hasPermission("chat.vote-to-mute")) {
            instances.values().stream()
                    .filter(activeVoteToMute -> previousServer == null || activeVoteToMute.getServer().getServerInfo().hashCode() == previousServer.getServerInfo().hashCode())
                    .forEach(inst -> inst.removeEligibleVoter(player.getUniqueId()));
        } else {
            instances.values().stream()
                    .filter(ActiveVoteToMute::countLowerRanks)
                    .filter(activeVoteToMute -> previousServer == null || activeVoteToMute.getServer().getServerInfo().hashCode() == previousServer.getServerInfo().hashCode())
                    .forEach(inst -> inst.removeEligibleVoter(player.getUniqueId()));
        }
    }

    public static void addPotentialVoter(Player player, ServerConnection server) {
        if (!player.hasPermission("chat.backup-vote-to-mute"))
            return;
        if (player.hasPermission("chat.vote-to-mute")) {
            instances.values().stream()
                    .filter(activeVoteToMute -> activeVoteToMute.getServer().getServerInfo().hashCode() == server.getServerInfo().hashCode())
                    .forEach(activeVoteToMute -> activeVoteToMute.addEligibleVoter(player));
        } else {
            instances.values().stream()
                    .filter(ActiveVoteToMute::countLowerRanks)
                    .filter(activeVoteToMute -> activeVoteToMute.getServer().getServerInfo().hashCode() == server.getServerInfo().hashCode())
                    .forEach(activeVoteToMute -> activeVoteToMute.addEligibleVoter(player));
        }
    }

    public ActiveVoteToMute(@NotNull Player votedPlayer, @NotNull RegisteredServer server, ProxyServer proxyServer, Duration duration,
                            int totalEligibleVoters, boolean countLowerRanks, Component chatLogs) {
        this.chatLogs = chatLogs;
        this.votedPlayer = votedPlayer;
        this.totalEligibleVoters = totalEligibleVoters;
        this.countLowerRanks = countLowerRanks;
        this.server = server;
        this.proxyServer = proxyServer;
        instances.put(votedPlayer.getUsername(), this);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(this::endVote,
                duration.toMinutes(), TimeUnit.MINUTES);
    }

    private RegisteredServer getServer() {
        return server;
    }

    private void endVote() {
        if (endedVote)
            return;
        instances.remove(votedPlayer.getUsername());
        if (votePassed()) {
            mutePlayer();
            return;
        }
        Component message = Utility.parseMiniMessage("<prefix> <red>The vote to mute <player> has failed, they will not be muted.</red>",
                Placeholder.component("prefix", prefix), Placeholder.parsed("player", votedPlayer.getUsername()));
        server.getPlayersConnected().stream()
                .filter(player -> countLowerRanks ? player.hasPermission("chat.backup-vote-to-mute") : player.hasPermission("chat.vote-to-mute"))
                .forEach(player -> player.sendMessage(message));
    }

    public void start() {
        Component message = getVoteStartMessage();
        server.getPlayersConnected().stream()
                .filter(player -> countLowerRanks ? player.hasPermission("chat.backup-vote-to-mute") : player.hasPermission("chat.vote-to-mute"))
                .forEach(player -> player.sendMessage(message));
    }

    public void vote(UUID uuid, boolean votedToMute) {
        if (votedToMute) {
            votedFor.add(uuid);
            votedAgainst.remove(uuid);
            if (!votePassed()) {
                return;
            }
            endedVote = true;
            instances.remove(votedPlayer.getUsername());
            mutePlayer();
        } else {
            votedAgainst.add(uuid);
            votedFor.remove(uuid);
        }
    }

    public boolean votePassed() {
        double totalVotes = (votedFor.size() + votedAgainst.size());
        if (totalVotes == 0 || votedFor.isEmpty()) {
            return false;
        }
        if (totalVotes / totalEligibleVoters < 0.6) {
            return false;
        }
        return votedFor.size() / totalVotes > 0.6;
    }

    public boolean countLowerRanks() {
        return countLowerRanks;
    }

    private void mutePlayer() {
        Component message = Utility.parseMiniMessage("<prefix> <green>The vote to mute <player> has passed, they will be muted.</green>",
                Placeholder.component("prefix", prefix), Placeholder.parsed("player", votedPlayer.getUsername()));
        server.getPlayersConnected().stream()
                .filter(player -> countLowerRanks ? player.hasPermission("chat.backup-vote-to-mute") : player.hasPermission("chat.vote-to-mute"))
                .forEach(player -> player.sendMessage(message));
        proxyServer.getCommandManager().executeAsync(proxyServer.getConsoleCommandSource(),
                String.format("tempmute %s 1h Muted by the community - under review.", votedPlayer.getUsername()));


        String chatLogsString = PlainTextComponentSerializer.plainText().serialize(chatLogs);
        EmbedBuilder embedBuilder = buildMutedEmbed(chatLogsString);

        ALogger.info(String.format("Player %s muted by vote\nLogs:\n%s\n\nVotes for:\n%s\nVotes against:\n%s\n",
                votedPlayer.getUsername(),
                chatLogsString,
                parseUUIDsToPlayerOrString(votedFor),
                parseUUIDsToPlayerOrString(votedAgainst)
                ));

        long id = Config.serverChannelId.get("general");
        DiscordLink.getPlugin().getBot().sendEmbedToDiscord(id, embedBuilder, -1);
    }

    @NotNull
    private EmbedBuilder buildMutedEmbed(String chatLogsString) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setAuthor(votedPlayer.getUsername(), null, "https://crafatar.com/avatars/" + votedPlayer.getUniqueId() + "?overlay");
        embedBuilder.setTitle("Player muted by vote");
        embedBuilder.setColor(Color.CYAN);
        embedBuilder.addField("Logs",
                chatLogsString.substring(0, Math.min(chatLogsString.length(), 1024)),
                false);
        embedBuilder.addField("Server",
                server.getServerInfo().getName().substring(0, 1).toUpperCase() + server.getServerInfo().getName().substring(1),
                false);
        return embedBuilder;
    }

    private String parseUUIDsToPlayerOrString(Collection<UUID> uuids) {
        return uuids.stream().map(uuid -> {
            Optional<Player> player = proxyServer.getPlayer(uuid);
            if (player.isPresent()) {
                return player.get().getUsername();
            }
            return uuid.toString();
        }).collect(Collectors.joining("\n"));
    }

    public void addEligibleVoter(Player player) {
        UUID uuid = player.getUniqueId();
        if (votedAgainst.contains(uuid) || votedFor.contains(uuid))
            return;
        totalEligibleVoters++;
        player.sendMessage(getVoteStartMessage());
    }

    public void removeEligibleVoter(UUID uuid) {
        if (votedFor.contains(uuid) || votedAgainst.contains(uuid))
            return;
        totalEligibleVoters--;
    }

    private Component getVoteStartMessage() {
        return Utility.parseMiniMessage(
                String.format("""
                        <prefix> <green>A vote to mute <player> for one hour has been started, please read the logs below before voting.</green>
                        <logs>
                        <prefix> Click: <click:run_command:'/votetomutehelper vote %s yes'><red>Mute</red></click> --- <click:run_command:'/votetomutehelper vote %s no'><yellow>Don't mute</yellow></click>""",
                        votedPlayer.getUsername(), votedPlayer.getUsername()),
                Placeholder.component("prefix", prefix),
                Placeholder.parsed("player", votedPlayer.getUsername()),
                Placeholder.component("logs", chatLogs));
    }
}
