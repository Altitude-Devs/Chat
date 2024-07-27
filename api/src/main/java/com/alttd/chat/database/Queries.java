package com.alttd.chat.database;

import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.*;
import com.alttd.chat.objects.channels.Channel;
import com.alttd.chat.util.ALogger;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class Queries {

    //Create Tables

    public static void createTables() {
        List<String> tables = new ArrayList<>();
        tables.add("CREATE TABLE IF NOT EXISTS ignored_users (`uuid` VARCHAR(36) NOT NULL, `ignored_uuid` VARCHAR(36) NOT NULL, PRIMARY KEY (`uuid`, `ignored_uuid`))");
        tables.add("CREATE TABLE IF NOT EXISTS parties (`id` INT NOT NULL AUTO_INCREMENT, `owner_uuid` VARCHAR(36) NOT NULL, `party_name` VARCHAR(36) NOT NULL, `password` VARCHAR(36), PRIMARY KEY (`id`))");
        tables.add("CREATE TABLE IF NOT EXISTS chat_users (`uuid` VARCHAR(36) NOT NULL, `party_id` INT NOT NULL, `toggled_channel` VARCHAR(36) NULL DEFAULT NULL, PRIMARY KEY (`uuid`))");
        tables.add("CREATE TABLE IF NOT EXISTS mails (`id` INT NOT NULL AUTO_INCREMENT, `uuid` VARCHAR(36) NOT NULL, `sender` VARCHAR(36) NOT NULL, `message` VARCHAR(256) NOT NULL, `sendtime` BIGINT default 0, `readtime` BIGINT default 0, PRIMARY KEY (`id`))");
        createNicknamesTable();
        createRequestedNicknamesTable();
        ChatLogQueries.createChatLogTable();
        try {
            Connection connection = DatabaseConnection.getConnection();

            for (String query : tables) {
                connection.prepareStatement(query).execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------

    //Nicknames
    private static void createNicknamesTable() {

        try {
            String nicknamesTableQuery = "CREATE TABLE IF NOT EXISTS nicknames("
                    + "uuid VARCHAR(48) NOT NULL,"
                    + "PRIMARY KEY (uuid))";
            DatabaseConnection.getConnection().prepareStatement(nicknamesTableQuery).executeUpdate();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        List<String> columns = new ArrayList<>();
        columns.add("ALTER TABLE nicknames ADD nickname VARCHAR(192)");
        columns.add("ALTER TABLE nicknames ADD date_changed BIGINT default 0");
        for (String string : columns) {
            try {
                DatabaseConnection.getConnection().prepareStatement(string).executeUpdate();
            } catch (Throwable ignored) {
            }
        }

    }

    private static void createRequestedNicknamesTable() {

        try {
            String requestedNicknamesTableQuery = "CREATE TABLE IF NOT EXISTS requested_nicknames("
                    + "uuid VARCHAR(48) NOT NULL,"
                    + "PRIMARY KEY (uuid))";
            DatabaseConnection.getConnection().prepareStatement(requestedNicknamesTableQuery).executeUpdate();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        List<String> columns = new ArrayList<>();
        columns.add("ALTER TABLE requested_nicknames ADD nickname VARCHAR(192)");
        columns.add("ALTER TABLE requested_nicknames ADD date_requested BIGINT default 0");
        for (String string : columns) {
            try {
                DatabaseConnection.getConnection().prepareStatement(string).executeUpdate();
            } catch (Throwable ignored) {
            }
        }

    }

    public static String getNickname(UUID uuid) {
        // View has been created.
        String query = "SELECT nickname FROM nicknames WHERE uuid = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getString("nickname");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //-----------------------------------------

    //Ignore

    /**
     * returns the UUID of all players this player has ignored.
     *
     * @param uuid the player who ignored the other players
     * @return List<UUID>
     */
    public static List<UUID> getIgnoredUsers(UUID uuid) {
        List<UUID> uuids = new ArrayList<>();
        String query = "SELECT * FROM ignored_users WHERE uuid = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                UUID ignoredUuid = UUID.fromString(resultSet.getString("ignored_uuid"));
                uuids.add(ignoredUuid);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uuids;
    }

    public static void ignoreUser(UUID uuid, UUID ignoredUuid) {
        //TODO this should be called from a function that already adds the user to the live ignored list, so this just adds it to the database

        String query = "INSERT INTO ignored_users (uuid, ignored_uuid) VALUES (?, ?)";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, uuid.toString());
            statement.setString(2, ignoredUuid.toString());

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void unIgnoreUser(UUID uuid, UUID ignoredUuid) {
        //TODO this should be called from a function that already removes the user from the live ignored list, so this just removes it from the database

        String query = "DELETE FROM ignored_users WHERE uuid = ? AND ignored_uuid = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, uuid.toString());
            statement.setString(2, ignoredUuid.toString());

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------

    //Party

    public static void loadParties() {
        String query = "SELECT * FROM parties";

        try {
            Connection connection = DatabaseConnection.getConnection();

            ResultSet resultSet = connection.prepareStatement(query).executeQuery();

            while (resultSet.next()) {

                int id = resultSet.getInt("id");
                UUID ownerUuid = UUID.fromString(resultSet.getString("owner_uuid"));
                String partyName = resultSet.getString("party_name");
                String password = resultSet.getString("password");

                PartyManager.addParty(new Party(id, ownerUuid, partyName, password));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        loadPartyUsers();
    }

    private static void loadPartyUsers() {
        String query = "SELECT chat_users.party_id, chat_users.uuid, nicknames.nickname, utility_users.Username " +
                "FROM chat_users " +
                    "LEFT OUTER JOIN nicknames ON chat_users.UUID = nicknames.uuid " +
                    "LEFT OUTER JOIN utility_users ON chat_users.uuid = utility_users.UUID " +
                "WHERE party_id != -1";

        try {
            Connection connection = DatabaseConnection.getConnection();

            ResultSet resultSet = connection.prepareStatement(query).executeQuery();

            while (resultSet.next()) {
                int id = resultSet.getInt("party_id");
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String displayName = resultSet.getString("nickname");
                if (displayName == null || displayName.isEmpty()) {
                    displayName = resultSet.getString("Username");
                }
                String playerName = resultSet.getString("Username");

                Party party = PartyManager.getParty(id);
                if (party == null) {
                    ALogger.warn("Unable to retrieve party: " + id);
                    continue;
                }
                party.putPartyUser(new PartyUser(uuid, displayName, playerName));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void loadPartyUsers(int id) {
        String query = "SELECT chat_users.uuid, nicknames.nickname, utility_users.Username " +
                "FROM chat_users " +
                "LEFT OUTER JOIN nicknames ON chat_users.UUID = nicknames.uuid " +
                "LEFT OUTER JOIN utility_users ON chat_users.uuid = utility_users.UUID " +
                "WHERE party_id = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            Party party = PartyManager.getParty(id);
            if (party == null) {
                ALogger.warn("Tried to load invalid party");
                return;
            }

            party.resetPartyUsers();
            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String displayName = resultSet.getString("nickname");
                if (displayName == null || displayName.isEmpty()) {
                    displayName = resultSet.getString("Username");
                }
                String playerName = resultSet.getString("Username");

                party.putPartyUser(new PartyUser(uuid, displayName, playerName));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Party addParty(UUID partyOwner, String partyName, String password) {
        String query = "INSERT INTO parties (owner_uuid, party_name, password) VALUES (?, ?, ?)";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, partyOwner.toString());
            statement.setString(2, partyName);
            statement.setString(3, password.isEmpty() ? null : password);

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return getParty(partyOwner, partyName, password);
    }

    private static Party getParty(UUID partyOwner, String partyName, String password) {
        String query = "SELECT * FROM parties WHERE owner_uuid = ? AND party_name = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, partyOwner.toString());
            statement.setString(2, partyName);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                int id = resultSet.getInt("id");

                return new Party(id, partyOwner, partyName, password);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void setPartyOwner(UUID uuid, int id) {
        setStringWhereId("UPDATE parties set owner_uuid = ? WHERE id = ?", uuid.toString(), id);
    }

    public static void setPartyName(String name, int id) {
        setStringWhereId("UPDATE parties set party_name = ? WHERE id = ?", name, id);
    }

    public static void setPartyPassword(String password, int id) {
        setStringWhereId("UPDATE parties set password = ? WHERE id = ?", password, id);
    }

    private static void setStringWhereId(String query, String string, int id) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, string);
            statement.setInt(2, id);

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addPartyUser(ChatUser user) {
        String query = "UPDATE chat_users SET party_id = ? WHERE uuid = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, user.getPartyId());
            statement.setString(2, user.getUuid().toString());

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeAllPartyUsers(ArrayList<PartyUser> partyUsers) {
        String query = "UPDATE chat_users SET party_id = -1 WHERE uuid = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            for (PartyUser partyUser : partyUsers) {
                statement.setString(1, partyUser.getUuid().toString());
                statement.addBatch();
            }

            statement.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removePartyUser(UUID uuid) {
        String query = "UPDATE chat_users SET party_id = -1 WHERE uuid = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, uuid.toString());

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removeParty(int id) {
        String deleteParty = "DELETE FROM parties WHERE id = ?";
        String updateUsers = "UPDATE chat_users SET party_id = -1 WHERE party_id = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(deleteParty);

            statement.setInt(1, id);
            statement.execute();

            statement = connection.prepareStatement(updateUsers);

            statement.setInt(1, id);
            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------

    public static ChatUser loadChatUser(UUID uuid) { //TODO Get parties from cache somewhere
        String query = "SELECT * FROM chat_users WHERE uuid = ?";
        ChatUser user = null;
        try {
            Connection connection = DatabaseConnection.getConnection();

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int partyId = resultSet.getInt("party_id");
                String toggledChannel = resultSet.getString("toggled_channel");
                Channel channel = toggledChannel == null ? null : Channel.getChatChannel(toggledChannel);
                user = new ChatUser(uuid, partyId, channel);
//                ChatUserManager.addUser(user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static void setToggledChannel(Channel channel, UUID uuid) {
        String sql = "UPDATE chat_users set toggled_channel = ? WHERE uuid = ?";
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, channel.getChannelName());
            statement.setString(2, uuid.toString());

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------

    public static List<Mail> getMails(UUID uuid) {
        List<Mail> mails = new ArrayList<>();
        String query = "SELECT * FROM mails where uuid = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, uuid.toString());

            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                UUID fromUUID = UUID.fromString(resultSet.getString("sender"));
                String message = resultSet.getString("message");
                long sendTime = resultSet.getLong("sendtime");
                long readTime = resultSet.getLong("readtime");
                mails.add(new Mail(id, uuid, fromUUID, sendTime, readTime, message));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mails;
    }

    public static void saveUser(ChatUser user) {
        String query = "INSERT INTO chat_users (uuid, party_id, toggled_channel) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE party_id = ?, toggled_channel = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            Channel toggledChannel = user.getToggledChannel();
            statement.setString(1, user.getUuid().toString());
            statement.setInt(2, user.getPartyId());
            statement.setString(3, toggledChannel == null ? null : toggledChannel.getChannelName());
            statement.setInt(4, user.getPartyId());
            statement.setString(5, toggledChannel == null ? null : toggledChannel.getChannelName());

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getDisplayName(UUID uuid) {
        String nickname = getNickname(uuid);
        if (nickname != null) return nickname;
        HashSet<String> userNames = getUserNames(List.of(uuid));
        return userNames.isEmpty() ? null : userNames.iterator().next();
    }

    public static HashSet<String> getUserNames(List<UUID> uuid) {
        StringBuilder query = new StringBuilder("SELECT Username FROM utility_users WHERE uuid IN (");
        if (uuid.isEmpty()) return new HashSet<>();

        query.append("?, ".repeat(uuid.size()));
        query.delete(query.length() - 2, query.length());
        query.append(")");

        HashSet<String> userNames = new HashSet<>();

        try {
            Connection connection = DatabaseConnection.getConnection();

            PreparedStatement statement = connection.prepareStatement(query.toString());

            for (int i = 0; i < uuid.size(); i++) {
                statement.setString(i + 1, uuid.get(i).toString());
            }

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                userNames.add(resultSet.getString("Username"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userNames;
    }

    public static UUID getPlayerUUID(String playerName) {
        String query = "SELECT UUID FROM utility_users WHERE Username  = ?";
        UUID uuid = null;
        try {
            Connection connection = DatabaseConnection.getConnection();

            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, playerName);

            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                uuid = UUID.fromString(resultSet.getString("UUID"));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return uuid;
    }

    public static int insertMail(Mail mail) {
        String query = "INSERT INTO mails (uuid , sender, message, sendtime, readtime) VALUES (?, ?, ?, ?, ?)";
        int id = 0;
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, mail.getUuid().toString());
            statement.setString(2, mail.getSender().toString());
            statement.setString(3, mail.getMessage());
            statement.setLong(4, mail.getSendTime());
            statement.setLong(5, mail.getReadTime());

            statement.execute();

            ResultSet rs = statement.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    public static void markMailRead(Mail mail) {
        String query = "INSERT INTO mails (Id, uuid , sender, message, sendtime, readtime) VALUES (?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE readtime = ?";
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, mail.getId());
            statement.setString(2, mail.getUuid().toString());
            statement.setString(3, mail.getSender().toString());
            statement.setString(4, mail.getMessage());
            statement.setLong(5, mail.getSendTime());
            statement.setLong(6, mail.getReadTime());
            statement.setLong(7, mail.getReadTime());

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Nicknames
    public static void setNicknameInDatabase(final UUID uuid, final String nickName) {
        final String sql = "INSERT INTO nicknames (uuid, nickname, date_changed) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE nickname = ?";
        try (final PreparedStatement statement = DatabaseConnection.getConnection().prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, nickName);
            statement.setLong(3, new java.util.Date().getTime());
            statement.setString(4, nickName);
            statement.execute();
        }
        catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void removePlayerFromDataBase(final UUID uuid) throws SQLException {
        final PreparedStatement insert = DatabaseConnection.getConnection().prepareStatement("DELETE FROM nicknames WHERE uuid = ?");
        insert.setString(1, uuid.toString());
        insert.executeUpdate();
        insert.close();
    }

    public static ArrayList<Nick> getNicknamesList() {
        ArrayList<Nick> nickList = new ArrayList<>();
        String queryNicknames = "SELECT uuid, nickname, date_changed FROM nicknames ";
        String queryRequests = "SELECT `requested_nicknames`.`nickname` AS new_nick, `requested_nicknames`.`date_requested`, " +
                "`nicknames`.`nickname` AS old_nick, `nicknames`.`date_changed`, `requested_nicknames`.`uuid` " +
                "FROM `requested_nicknames`" +
                "LEFT JOIN `nicknames` ON `requested_nicknames`.`uuid` = `nicknames`.`uuid` ";

        try {
            ResultSet resultSetNicknames = getStringResult(queryNicknames);
            while (resultSetNicknames.next()){
                nickList.add(new Nick(
                        UUID.fromString(resultSetNicknames.getString("uuid")),
                        resultSetNicknames.getString("nickname"),
                        resultSetNicknames.getLong("date_changed")));
            }

            ResultSet resultSetRequests = getStringResult(queryRequests);
            while (resultSetRequests.next()){
                nickList.add(new Nick(
                        UUID.fromString(resultSetRequests.getString("uuid")),
                        resultSetRequests.getString("old_nick"),
                        resultSetRequests.getLong("date_changed"),
                        resultSetRequests.getString("new_nick"),
                        resultSetRequests.getLong("date_requested")));
            }
        } catch (SQLException e) {
            ALogger.warn("Failed to get nicknames list\n" + Arrays.toString(e.getStackTrace())
                    .replace(",", "\n"));
        }
        return nickList;
    }

    public static Nick getNick(UUID uniqueId) {
        String getNick = "SELECT nickname, date_changed, uuid FROM nicknames WHERE uuid = ?";
        String getRequest = "SELECT nickname, date_requested, uuid FROM requested_nicknames WHERE uuid = ?";

        try {
            ResultSet resultSetNick = getStringResult(getNick, uniqueId.toString());
            ResultSet resultSetRequest = getStringResult(getRequest, uniqueId.toString());
            String uuid = null;
            String currentNick = null;
            long dateChanged = 0;
            String requestedNick = null;
            long dateRequested = 0;

            if (resultSetNick.next()) {
                uuid = resultSetNick.getString("uuid");
                currentNick = resultSetNick.getString("nickname");
                dateChanged = resultSetNick.getLong("date_changed");
            }
            if (resultSetRequest.next()) {
                uuid = resultSetRequest.getString("uuid");
                requestedNick = resultSetRequest.getString("nickname");
                dateRequested = resultSetRequest.getLong("date_requested");
            }
            if (uuid != null) {
                return new Nick(UUID.fromString(uuid), currentNick, dateChanged, requestedNick, dateRequested);
            }
        } catch (SQLException e){
            ALogger.warn("Failed to get nicknames for "
                    + uniqueId.toString() + "\n" + Arrays.toString(e.getStackTrace())
                    .replace(",", "\n"));

        }
        return null;
    }

    public static void denyNewNickname(UUID uniqueId) {
        String query = "DELETE FROM requested_nicknames WHERE uuid = ?";
        try {
            PreparedStatement statement = DatabaseConnection.getConnection().prepareStatement(query);
            statement.setString(1, uniqueId.toString());
            statement.execute();
        } catch (SQLException e) {
            ALogger.warn("Failed to delete requested nickname for "
                    + uniqueId.toString() + "\n" + Arrays.toString(e.getStackTrace())
                    .replace(",", "\n"));
        }
    }

    public static void acceptNewNickname(UUID uniqueId, String newNick){
        String delete = "DELETE FROM requested_nicknames WHERE uuid = ?";
        String update = "INSERT INTO nicknames (uuid, nickname, date_changed) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE nickname = ?, date_changed = ?";
        long time = new java.util.Date().getTime();

        try {
            PreparedStatement deleteStatement = DatabaseConnection.getConnection().prepareStatement(delete);
            deleteStatement.setString(1, uniqueId.toString());

            deleteStatement.execute();

            PreparedStatement updateStatement = DatabaseConnection.getConnection().prepareStatement(update);
            updateStatement.setString(1, uniqueId.toString());
            updateStatement.setString(2, newNick);
            updateStatement.setLong(3, time);
            updateStatement.setString(4, newNick);
            updateStatement.setLong(5, time);

            updateStatement.execute();
        } catch (SQLException e) {
            ALogger.warn("Failed to accept requested nickname for "
                    + uniqueId.toString() + "\n" + Arrays.toString(e.getStackTrace())
                    .replace(",", "\n"));
        }
    }

    public static void newNicknameRequest(UUID uniqueId, String nickName) {
        String requestQuery = "INSERT INTO requested_nicknames (uuid, nickname, date_requested) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE nickname = ?, date_requested = ?";
        String nickQuery = "INSERT INTO nicknames (uuid, nickname, date_changed) VALUES (?, ?, 0) " +
                "ON DUPLICATE KEY UPDATE uuid = uuid";
        long time = new Date().getTime();

        try {
            PreparedStatement requestPreparedStatement = DatabaseConnection.getConnection().prepareStatement(requestQuery);
            requestPreparedStatement.setString(1, uniqueId.toString());
            requestPreparedStatement.setString(2, nickName);
            requestPreparedStatement.setLong(3, time);
            requestPreparedStatement.setString(4, nickName);
            requestPreparedStatement.setLong(5, time);

            requestPreparedStatement.execute();

            PreparedStatement nickPreparedStatement = DatabaseConnection.getConnection().prepareStatement(nickQuery);
            nickPreparedStatement.setString(1, uniqueId.toString());
            nickPreparedStatement.setString(2, null);

            nickPreparedStatement.execute();

        } catch (SQLException e) {
            ALogger.warn("Failed to store requested nickname for "
                    + uniqueId.toString() + "\n" + Arrays.toString(e.getStackTrace())
                    .replace(",", "\n"));
        }
    }

    private static ResultSet getStringResult(final String query, final String... parameters) throws SQLException {
        final PreparedStatement statement = DatabaseConnection.getConnection().prepareStatement(query);
        for (int i = 1; i < parameters.length + 1; ++i) {
            statement.setString(i, parameters[i - 1]);
        }
        return statement.executeQuery();
    }
}
