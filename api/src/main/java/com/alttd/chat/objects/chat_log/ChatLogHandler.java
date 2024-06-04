package com.alttd.chat.objects.chat_log;

import com.alttd.chat.config.Config;
import com.alttd.chat.database.ChatLogQueries;
import com.alttd.chat.util.ALogger;
import org.jetbrains.annotations.NotNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class ChatLogHandler {

    private static ChatLogHandler instance = null;
    private ScheduledExecutorService executorService = null;

    public static ChatLogHandler getInstance(boolean enableLogging) {
        if (instance == null)
            instance = new ChatLogHandler(enableLogging);
        return instance;
    }

    private boolean isSaving;
    private final Queue<ChatLog> chatLogQueue = new ConcurrentLinkedQueue<>();
    private final HashMap<UUID, List<ChatLog>> chatLogs = new HashMap<>();

    public ChatLogHandler(boolean enableLogging) {
        if (!enableLogging) {
            ALogger.info("Logging is not enabled on this server.");
            return;
        }
        Duration deleteThreshold = Duration.ofDays(Config.CHAT_LOG_DELETE_OLDER_THAN_DAYS);
        ChatLogQueries.deleteOldMessages(deleteThreshold).thenAccept(success -> {
            if (success) {
                ALogger.info(String.format("Deleted all messages older than %s days from chat log database.", deleteThreshold.toDays()));
            } else {
                ALogger.warn(String.format("Failed to delete all messages older than %s days from chat log database.", deleteThreshold.toDays()));
            }
        });
        executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleAtFixedRate(() -> {
                    saveToDatabase(false);
                    ALogger.info(String.format("Running scheduler to save messages with a %d delay", Config.CHAT_LOG_SAVE_DELAY_MINUTES));
                },
                Config.CHAT_LOG_SAVE_DELAY_MINUTES, Config.CHAT_LOG_SAVE_DELAY_MINUTES, TimeUnit.MINUTES);
        ALogger.info("Logging has started!");
    }

    /**
     * Shuts down the executor service and saves the chat logs to the database.
     * Will throw an error if called on a ChatLogHandler that was started without logging
     */
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
        ALogger.info(String.format("Saving %d messages to database", chatLogs.size()));
        CompletableFuture<Boolean> booleanCompletableFuture = ChatLogQueries.storeMessages(chatLogs);
        if (onMainThread) {
            booleanCompletableFuture.join();
            ALogger.info("Finished saving messages on main thread");
            return;
        }
        booleanCompletableFuture.whenComplete((result, throwable) -> {
            if (throwable == null && result) {
                chatLogs.clear();
            } else {
                ALogger.error("Failed to save chat messages.");
            }
            savingToDatabase(false);
            if (!chatLogQueue.isEmpty()) {
                ALogger.info("Adding back messages from queue to chatLogs map");
            }
            while (!chatLogQueue.isEmpty()) {
                addLog(chatLogQueue.remove());
            }
            ALogger.info("Finished saving messages");
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
