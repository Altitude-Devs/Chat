package com.alttd.chat.objects.chat_log;

import com.alttd.chat.config.Config;
import com.alttd.chat.database.ChatLogQueries;
import com.alttd.chat.util.ALogger;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.*;

public class ChatLogHandler {

    private static ChatLogHandler instance = null;
    private final ScheduledExecutorService executorService;

    public static ChatLogHandler getInstance() {
        if (instance == null)
            instance = new ChatLogHandler();
        return instance;
    }

    private boolean isSaving;
    private final Queue<ChatLog> chatLogQueue = new ConcurrentLinkedQueue<>();
    private final Object2ObjectOpenHashMap<UUID, List<ChatLog>> chatLogs = new Object2ObjectOpenHashMap<>();

    public ChatLogHandler() {
        Duration deleteThreshold = Duration.ofDays(Config.CHAT_LOG_DELETE_OLDER_THAN_DAYS);
        ChatLogQueries.deleteOldMessages(deleteThreshold).thenAccept(success -> {
            if (success) {
                ALogger.info(String.format("Deleted all messages older than %s days from chat log database.", deleteThreshold.toDays()));
            } else {
                ALogger.warn(String.format("Failed to delete all messages older than %s days from chat log database.", deleteThreshold.toDays()));
            }
        });
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> saveToDatabase(false),
                Config.CHAT_LOG_SAVE_DELAY_MINUTES, Config.CHAT_LOG_SAVE_DELAY_MINUTES, TimeUnit.MINUTES);
    }

    public void shutDown() {
        executorService.shutdown();
        saveToDatabase(true);
    }

    private synchronized void savingToDatabase(boolean saving) {
        isSaving = saving;
    }

    private synchronized boolean isBlocked() {
        return isSaving;
    }

    public synchronized void addLog(ChatLog chatLog) {
        if (isBlocked()) {
            chatLogQueue.add(chatLog);
        } else {
            chatLogs.computeIfAbsent(chatLog.getUuid(), k -> new ArrayList<>()).add(chatLog);
        }
    }

    private void saveToDatabase(boolean onMainThread) {
        savingToDatabase(true);
        CompletableFuture<Boolean> booleanCompletableFuture = ChatLogQueries.storeMessages(chatLogs);
        if (onMainThread) {
            booleanCompletableFuture.join();
            return;
        }
        booleanCompletableFuture.whenComplete((result, throwable) -> {
            if (throwable == null && result) {
                chatLogs.clear();
            }
            savingToDatabase(false);
            while (!chatLogQueue.isEmpty()) {
                addLog(chatLogQueue.remove());
            }
        });
    }

    public ChatLog loadFromResultSet(@NotNull ResultSet resultSet) throws SQLException {
        UUID chatLogUUID = UUID.fromString(resultSet.getString("uuid"));
        Instant chatTimestamp = resultSet.getTimestamp("time_stamp").toInstant();
        String server = resultSet.getString("server");
        String chatMessage = resultSet.getString("chat_message");
        boolean chatMessageBlocked = resultSet.getInt("blocked") == 1;
        return new ChatLog(chatLogUUID, chatTimestamp, server, chatMessage, chatMessageBlocked);
    }

    public void addChatLog(UUID uuid, String server, String message, boolean blocked) {
        addLog(new ChatLog(uuid, Instant.now(), server, message, blocked));
    }

    public CompletableFuture<List<ChatLog>> retrieveChatLogs(UUID uuid, Duration duration, String server) {
        List<ChatLog> chatLogList = chatLogs.getOrDefault(uuid, new ArrayList<>());
        return ChatLogQueries.retrieveMessages(this, uuid, duration, server)
                .thenCompose(chatLogs -> CompletableFuture.supplyAsync(() -> {
                    chatLogList.addAll(chatLogs);
                    return chatLogList;
                }))
                .exceptionally(ex -> {
                    throw new CompletionException(ex);
                });
    }

}
