package com.alttd.chat.database;

import com.alttd.chat.managers.ChatUserManager;
import com.alttd.chat.managers.PartyManager;
import com.alttd.chat.objects.ChatUser;
import com.alttd.chat.objects.Mail;
import com.alttd.chat.objects.Party;
import com.alttd.chat.util.ALogger;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.List;

public class Queries {

    //Create Tables

    public static void createTables() {
        List<String> tables = new ArrayList<>();
        tables.add("CREATE TABLE IF NOT EXISTS ignored_users (`uuid` VARCHAR(36) NOT NULL, `ignored_uuid` VARCHAR(36) NOT NULL, PRIMARY KEY (`uuid`, `ignored_uuid`))");
        tables.add("CREATE TABLE IF NOT EXISTS parties (`id` INT NOT NULL AUTO_INCREMENT, `owner_uuid` VARCHAR(36) NOT NULL, `party_name` VARCHAR(36) NOT NULL, `password` VARCHAR(36), PRIMARY KEY (`id`))");
        tables.add("CREATE TABLE IF NOT EXISTS chat_users (`uuid` VARCHAR(36) NOT NULL, `party_id` INT NOT NULL, `toggled_chat` BIT(1) DEFAULT b'0', `toggled_gc` BIT(1) DEFAULT b'0', PRIMARY KEY (`uuid`))");
        tables.add("CREATE TABLE IF NOT EXISTS mails (`id` INT NOT NULL AUTO_INCREMENT, `uuid` VARCHAR(36) NOT NULL, `from` VARCHAR(36) NOT NULL, `message` VARCHAR(256) NOT NULL, `sendtime` BIGINT default 0, `readtime` BIGINT default 0, PRIMARY KEY (`id`))");

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
//                String displayName = resultSet.getString("nickname");
//                if (displayName == null || displayName.isEmpty()) {
//                    displayName = resultSet.getString("Username");
//                }
                String displayName = resultSet.getString("Username"); // FIXME: 08/08/2021 only using display name till we can fix nickname colors

                Party party = PartyManager.getParty(id);
                if (party == null) {
                    ALogger.warn("Unable to retrieve party: " + id);
                    continue;
                }

                party.putUser(uuid, displayName);
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

    public static void loadChatUsers() { //TODO Get parties from cache somewhere
        String query = "SELECT * FROM chat_users WHERE party_id > -1";

        try {
            Connection connection = DatabaseConnection.getConnection();

            ResultSet resultSet = connection.prepareStatement(query).executeQuery();

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                int partyId = resultSet.getInt("party_id");
                boolean toggled_chat = resultSet.getInt("toggled_chat") == 1;
                boolean toggle_Gc = resultSet.getInt("toggled_gc") == 1;
                ChatUserManager.addUser(new ChatUser(uuid, partyId, toggled_chat, toggle_Gc));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
                boolean toggled_chat = resultSet.getInt("toggled_chat") == 1;
                boolean toggle_Gc = resultSet.getInt("toggled_gc") == 1;
                user = new ChatUser(uuid, partyId, toggled_chat, toggle_Gc);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return user;
    }

    public static void setPartyChatState(boolean toggledChat, UUID uuid) {
        setBitWhereId("UPDATE chat_users set toggled_chat = ? WHERE uuid = ?", toggledChat, uuid);
    }

    public static void setGlobalChatState(boolean globalChat, UUID uuid) {
        setBitWhereId("UPDATE chat_users set toggled_gc = ? WHERE uuid = ?", globalChat, uuid);
    }

    private static void setBitWhereId(String query, boolean bool, UUID uuid) {
        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, bool ? 1 : 0);
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
                UUID fromUUID = UUID.fromString(resultSet.getString("from"));
                String message = resultSet.getString("message");
                long sendTime = resultSet.getLong("sendtime");
                long readTime = resultSet.getLong("readtime");
                mails.add(new Mail(uuid, fromUUID, sendTime, readTime, message));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return mails;
    }

    public static void saveUser(ChatUser user) {
        String query = "INSERT INTO chat_users (uuid, party_id, toggled_chat, toggled_gc) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE party_id = ?, toggled_chat = ?, toggled_gc = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, user.getUuid().toString());
            statement.setInt(2, user.getPartyId());
            statement.setInt(3, user.toggledPartyChat() ? 1 : 0);
            statement.setInt(4, 0);
            statement.setInt(5, user.getPartyId());
            statement.setInt(6, user.toggledPartyChat() ? 1 : 0);
            statement.setInt(7, 0);

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
}
