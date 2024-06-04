package com.alttd.chat.objects.chat_log;

import com.alttd.chat.objects.BatchInsertable;
import org.jetbrains.annotations.NotNull;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

public class ChatLog implements BatchInsertable {

    private final UUID uuid;
    private final Instant timestamp;
    private final String server;
    private final String message;
    private final boolean blocked;

    protected ChatLog(UUID uuid, Instant timestamp, String server, String message, boolean blocked) {
        this.uuid = uuid;
        this.timestamp = timestamp;
        this.server = server;
        this.message = message;
        this.blocked = blocked;
    }

    @Override
    public void prepareStatement(@NotNull PreparedStatement preparedStatement) throws SQLException {
        preparedStatement.setString(1, uuid.toString());
        preparedStatement.setTimestamp(2, Timestamp.from(timestamp));
        preparedStatement.setString(3, server);
        preparedStatement.setString(4, message);
        preparedStatement.setInt(5, blocked ? 1 : 0);
    }

    public UUID getUuid() {
        return uuid;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public boolean isBlocked() {
        return blocked;
    }
}
