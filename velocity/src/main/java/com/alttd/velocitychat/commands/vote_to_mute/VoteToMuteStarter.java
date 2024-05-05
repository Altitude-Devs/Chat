package com.alttd.velocitychat.commands.vote_to_mute;

import com.alttd.chat.objects.chat_log.ChatLog;
import com.alttd.chat.objects.chat_log.ChatLogHandler;
import com.alttd.chat.util.Utility;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class VoteToMuteStarter {

    private static final HashMap<UUID, VoteToMuteStarter> instanceMap = new HashMap<>();

    private static final Component prefix = Utility.parseMiniMessage("<gold>[VoteMute]</gold>");
    private final ChatLogHandler chatLogHandler;
    private final Player votedPlayer;
    private final Player commandSource;
    private final String serverName;
    private List<Component> parsedChatLogs;
    private final boolean countLowerRanks;

    public static Optional<VoteToMuteStarter> getInstance(UUID uuid) {
        if (!instanceMap.containsKey(uuid))
            return Optional.empty();
        return Optional.of(instanceMap.get(uuid));
    }

    public VoteToMuteStarter(ChatLogHandler chatLogHandler, Player votedPlayer, Player commandSource, String serverName, boolean countLowerRanks) {
        this.chatLogHandler = chatLogHandler;
        this.votedPlayer = votedPlayer;
        this.commandSource = commandSource;
        this.serverName = serverName;
        this.countLowerRanks = countLowerRanks;
        instanceMap.put(commandSource.getUniqueId(), this);
    }

    public void start() {
        chatLogHandler.retrieveChatLogs(votedPlayer.getUniqueId(), Duration.ofMinutes(10), serverName).whenCompleteAsync((chatLogs, throwable) -> {
            if (throwable != null) {
                commandSource.sendMessage(Utility.parseMiniMessage("<prefix> <red>Unable to retrieve messages</red> for player <player>",
                        Placeholder.component("prefix", prefix),
                        Placeholder.parsed("player", votedPlayer.getUsername())));
                return;
            }
            parseChatLogs(chatLogs);
            commandSource.sendMessage(Utility.parseMiniMessage(
                    "<prefix> <green>Please select up to 10 messages other players should see to decide their vote, seperated by comma's. " +
                            "Example: <gold>/votetomutehelper messages 1, 2, 5, 8</gold></green>", Placeholder.component("prefix", prefix)));
            showPage(1);
        });
    }

    private void parseChatLogs(List<ChatLog> chatLogs) {
        TagResolver.Single playerTag = Placeholder.parsed("player", votedPlayer.getUsername());
        TagResolver.Single prefixTag = Placeholder.component("prefix", prefix);
        chatLogs.sort(Comparator.comparing(ChatLog::getTimestamp));
        parsedChatLogs = IntStream.range(0, chatLogs.size())
                .mapToObj(i -> Utility.parseMiniMessage(
                        "<number>. <prefix> <player>: <message>",
                        TagResolver.resolver(
                                Placeholder.unparsed("message", chatLogs.get(i).getMessage()),
                                Placeholder.parsed("number", String.valueOf(i + 1)),
                                playerTag, prefixTag
                        ))
                )
                .toList();
    }

    public void showPage(int page) {
        List<Component> collect = parsedChatLogs.stream().skip((page - 1) * 10L).limit(10L).toList();
        Component chatLogsComponent = Component.join(JoinConfiguration.newlines(), collect);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<prefix> ChatLogs for <player>\n<logs>\n");
        if (page > 1) {
            stringBuilder.append("<click:run_command:/votetomutehelper page ")
                    .append(page - 1)
                    .append("><hover:show_text:'<gold>Click to go to previous page'><gold><previous page></gold></hover></click> ");
        }
        if (parsedChatLogs.size() > page * 10) {
            stringBuilder.append("<click:run_command:/votetomutehelper page ")
                    .append(page + 1)
                    .append("><hover:show_text:'<gold>Click to go to next page'><gold><next page></gold></hover></click> ");
        }
        commandSource.sendMessage(Utility.parseMiniMessage(stringBuilder.toString(),
                Placeholder.parsed("player", votedPlayer.getUsername()),
                Placeholder.component("prefix", prefix),
                Placeholder.component("logs", chatLogsComponent)));
    }

    /**
     * Retrieves the chat logs for the given list of IDs. It removes 1 from the IDs before using them
     * It removes the instance from the hashmap after this function call
     *
     * @param ids A list of integers representing the IDs of the chat logs to retrieve.
     * @return A Component object containing the selected chat logs joined by newlines.
     */
    public Component getChatLogsAndClose(List<Integer> ids) {
        List<Component> selectedChatLogs = ids.stream()
                .filter(id -> id >= 1 && id <= parsedChatLogs.size())
                .map(id -> parsedChatLogs.get(id - 1))
                .collect(Collectors.toList());

        instanceMap.remove(commandSource.getUniqueId());
        return Component.join(JoinConfiguration.newlines(), selectedChatLogs);
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) parsedChatLogs.size() / 10);
    }

    public Player getVotedPlayer() {
        return votedPlayer;
    }

    public int getTotalLogEntries() {
        return parsedChatLogs.size();
    }

    public boolean countLowerRanks() {
        return countLowerRanks;
    }
}
