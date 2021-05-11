package com.alttd.chat.database;

import com.alttd.chat.objects.Party;
import com.alttd.chat.objects.PartyUser;
import com.alttd.chat.objects.Regex;
import com.alttd.chat.objects.RegexType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class Queries {

    //Create Tables

    public static void createTables() {
        List<String> tables = new ArrayList<>();
        tables.add("CREATE TABLE IF NOT EXISTS regex (`id` INT NOT NULL AUTO_INCREMENT, `regex` VARCHAR(2048) NOT NULL, `type` VARCHAR(32) NOT NULL, `replacement` VARCHAR(256) NULL, PRIMARY KEY (`id`))");
        tables.add("CREATE TABLE IF NOT EXISTS ignored_users (`uuid` VARCHAR(36) NOT NULL, `ignored_uuid` VARCHAR(36) NOT NULL, PRIMARY KEY (`uuid`, `ignored_uuid`))");
        tables.add("CREATE TABLE IF NOT EXISTS parties (`id` INT NOT NULL AUTO_INCREMENT, `owner_uuid` VARCHAR(36) NOT NULL, `party_name` VARCHAR(36) NOT NULL, `password` VARCHAR(36), PRIMARY KEY (`id`))");
        tables.add("CREATE TABLE IF NOT EXISTS party_users (`uuid` VARCHAR(36) NOT NULL, `party_id` INT NOT NULL, `toggled_chat` BIT(1) DEFAULT b'0', `force_tp` BIT(1) DEFAULT b'1', PRIMARY KEY (`uuid`), FOREIGN KEY (party_id) REFERENCES parties(id) ON DELETE CASCADE)");

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

    //Regex

    public static Regex getRegex() {
        String query = "SELECT * FROM regex";

        try {
            Connection connection = DatabaseConnection.getConnection();

            ResultSet resultSet = connection.prepareStatement(query).executeQuery();

            while (resultSet.next()) {
                String regex = resultSet.getString("regex");
                RegexType type = RegexType.getType(resultSet.getString("type"));

                if (regex.isEmpty() || type == null) {
                    //TODO log this properly
                    System.out.println("INCORRECT LOGGING: regex was empty or type was invalid when getting from the database");
                    continue;
                }

                if (type.equals(RegexType.REPLACE)) {
                    String replacement = resultSet.getString("replacement");
                    //TODO Add to list in util\Regex.java
                    new Regex(regex, type, replacement);
                } else {
                    //TODO Add to list in util\Regex.java
                    new Regex(regex, type);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    //-----------------------------------------

    //Ignore

    public static void getIgnoredUsers() { //TODO store this in a user object or something?
        HashMap<UUID, ArrayList<UUID>> ignoredUsers = new HashMap<>(); //TODO Replace with a proper way/location to store this in
        String query = "SELECT * FROM ignored_users";

        try {
            Connection connection = DatabaseConnection.getConnection();

            ResultSet resultSet = connection.prepareStatement(query).executeQuery();

            while (resultSet.next()) {
                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                UUID ignoredUuid = UUID.fromString(resultSet.getString("ignored_uuid"));
                ArrayList<UUID> uuids;

                if (ignoredUsers.containsKey(uuid)) {
                    uuids = ignoredUsers.get(uuid);
                } else {
                    uuids = new ArrayList<>();
                }

                uuids.add(ignoredUuid);
                ignoredUsers.put(uuid, uuids);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    public static void getParties() {
        HashMap<Integer, Party> parties = new HashMap<>(); //TODO Replace with a proper way/location to store this in
        String query = "SELECT * FROM parties";

        try {
            Connection connection = DatabaseConnection.getConnection();

            ResultSet resultSet = connection.prepareStatement(query).executeQuery();

            while (resultSet.next()) {

                int id = resultSet.getInt("id");
                UUID ownerUuid = UUID.fromString(resultSet.getString("owner_uuid"));
                String partyName = resultSet.getString("party_name");
                String password = resultSet.getString("password");

                parties.put(id, new Party(id, ownerUuid, partyName, password));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        getPartyUsers(parties); //TODO This parameter should be temporary, it should access the actual list of parties normally
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

    public static void removeParty(int id) {
        String query = "DELETE FROM parties WHERE id = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setInt(1, id);

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------

    //Party Users

    private static void getPartyUsers(HashMap<Integer, Party> parties) {
        String query = "SELECT * FROM party_users";

        try {
            Connection connection = DatabaseConnection.getConnection();

            ResultSet resultSet = connection.prepareStatement(query).executeQuery();

            while (resultSet.next()) {

                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                int partyId = resultSet.getInt("party_id");
                boolean toggled_chat = resultSet.getInt("toggled_chat") == 1;
                boolean force_tp = resultSet.getInt("force_tp") == 1;

                Party party = parties.get(partyId);
                if (party == null) {
                    //TODO log this properly
                    System.out.println("INCORRECT LOGGING: party was empty, the party id stored in the database with user " + uuid + " was invalid.");
                    continue;
                }

                party.addUser(new PartyUser(uuid, partyId, toggled_chat, force_tp));

            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void addUser(PartyUser user) {
        String query = "INSERT INTO party_users (uuid, party_id, toggled_chat, force_tp) VALUES (?, ?, ?, ?)";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, user.getUuid().toString());
            statement.setInt(2, user.getPartyId());
            statement.setInt(3, user.toggledChat() ? 1 : 0);
            statement.setInt(4, user.ForceTp() ? 1 : 0);

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void setChatState(boolean toggledChat, UUID uuid) {
        setBitWhereId("UPDATE party_users set toggled_chat = ? WHERE uuid = ?", toggledChat, uuid);
    }

    public static void setForceTpState(boolean forceTp, UUID uuid) {
        setBitWhereId("UPDATE party_users set force_tp = ? WHERE uuid = ?", forceTp, uuid);
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

    public static void removeUser(UUID uuid) {
        String query = "DELETE FROM party_users WHERE uuid = ?";

        try {
            Connection connection = DatabaseConnection.getConnection();
            PreparedStatement statement = connection.prepareStatement(query);

            statement.setString(1, uuid.toString());

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //-----------------------------------------
}
