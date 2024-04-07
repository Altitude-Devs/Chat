package com.alttd.chat.database;

import com.alttd.chat.objects.chat_log.ChatLog;
import com.alttd.chat.objects.chat_log.ChatLogHandler;
import com.alttd.chat.util.ALogger;
import org.jetbrains.annotations.NotNull;

import java.sql.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public class ChatLogQueries {

    protected static void createChatLogTable() {
        String nicknamesTableQuery = "CREATE TABLE IF NOT EXISTS chat_log("
                + "uuid CHAR(48) NOT NULL,"
                + "time_stamp TIMESTAMP(6) NOT NULL, "
                + "server VARCHAR(50) NOT NULL, "
                + "chat_message VARCHAR(300) NOT NULL, "
                + "blocked BIT(1) NOT NULL DEFAULT 0"
                + ")";

        try (PreparedStatement preparedStatement = DatabaseConnection.getConnection().prepareStatement(nicknamesTableQuery)) {
            preparedStatement.executeUpdate();
        } catch (Throwable throwable) {
            ALogger.error("Failed to create chat log table", throwable);
        }
    }

    public static @NotNull CompletableFuture<Boolean> storeMessages(HashMap<UUID, List<ChatLog>> chatMessages) {
        String insertQuery = "INSERT INTO chat_log (uuid, time_stamp, server, chat_message, blocked) VALUES (?, ?, ?, ?, ?)";
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DatabaseConnection.createTransactionConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);
                for (List<ChatLog> chatLogList : chatMessages.values()) {
                    for (ChatLog chatLog : chatLogList) {
                        chatLog.prepareStatement(preparedStatement);
                        preparedStatement.addBatch();
                    }
                }
                int[] updatedRowsCount = preparedStatement.executeBatch();
                boolean isSuccess = Arrays.stream(updatedRowsCount).allMatch(i -> i >= 0);

                if (isSuccess) {
                    connection.commit();
                    return true;
                } else {
                    connection.rollback();
                    ALogger.warn("Failed to store messages");
                    return false;
                }
            } catch (SQLException sqlException) {
                ALogger.error("Failed to store chat messages", sqlException);
                throw new CompletionException("Failed to store chat messages", sqlException);
            }
        });
    }

    public static @NotNull CompletableFuture<List<ChatLog>> retrieveMessages(ChatLogHandler chatLogHandler, UUID uuid, Duration duration, String server) {
        String query = "SELECT * FROM chat_log WHERE uuid = ? AND time_stamp > ? AND server = ?";
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DatabaseConnection.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setString(1, uuid.toString());
                preparedStatement.setTimestamp(2, Timestamp.from(Instant.now().minus(duration)));
                preparedStatement.setString(3, server);
                ResultSet resultSet = preparedStatement.executeQuery();
                List<ChatLog> chatLogs = new ArrayList<>();
                while (resultSet.next()) {
                    ChatLog chatLog = chatLogHandler.loadFromResultSet(resultSet);
                    chatLogs.add(chatLog);
                }
                return chatLogs;
            } catch (SQLException sqlException) {
                ALogger.error(String.format("Failed to retrieve messages for user %s", uuid), sqlException);
                throw new CompletionException(String.format("Failed to retrieve messages for user %s", uuid), sqlException);
            }
        });
    }

    public static CompletableFuture<Boolean> deleteOldMessages(Duration duration) {
        String query = "DELETE FROM chat_log WHERE time_stamp > ?";

        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = DatabaseConnection.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                preparedStatement.setTimestamp(1, Timestamp.from(Instant.now().minus(duration)));
                return preparedStatement.execute();
            } catch (SQLException sqlException) {
                ALogger.error(String.format("Failed to delete messages older than %s days", duration.toDays()), sqlException);
                throw new CompletionException(String.format("Failed to delete messages older than %s days", duration.toDays()), sqlException);
            }
        });
    }
}
