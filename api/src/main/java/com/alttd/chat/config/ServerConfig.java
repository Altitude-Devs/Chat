package com.alttd.chat.config;

import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.regex.Pattern;

public final class ServerConfig {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");

    private final String serverName;
    private final String configPath;
    private final String defaultPath;

    public ServerConfig(String serverName) {
        this.serverName = serverName;
        this.configPath = "server-settings." + this.serverName + ".";
        this.defaultPath = "server-settings.default.";
        init();
    }

    public void init() {
        Config.readConfig(ServerConfig.class, this);
        Config.saveConfig();
    }

    public static Object[] splitPath(String key) {
        return PATH_PATTERN.split(key);
    }

    private static void set(String path, Object def) {
        if(Config.config.node(splitPath(path)).virtual()) {
            try {
                Config.config.node(splitPath(path)).set(def);
            } catch (SerializationException ex) {
            }
        }
    }

    private static void setString(String path, String def) {
        try {
            if(Config.config.node(splitPath(path)).virtual())
                Config.config.node(splitPath(path)).set(TypeToken.get(String.class), def);
        } catch(SerializationException ex) {
        }
    }

    private boolean getBoolean(String path, boolean def) {
        set(defaultPath +path, def);
        return Config.config.node(splitPath(configPath+path)).getBoolean(
                Config.config.node(splitPath(defaultPath +path)).getBoolean(def));
    }

    private double getDouble(String path, double def) {
        set(defaultPath +path, def);
        return Config.config.node(splitPath(configPath+path)).getDouble(
                Config.config.node(splitPath(defaultPath +path)).getDouble(def));
    }

    private int getInt(String path, int def) {
        set(defaultPath +path, def);
        return Config.config.node(splitPath(configPath+path)).getInt(
                Config.config.node(splitPath(defaultPath +path)).getInt(def));
    }

    private String getString(String path, String def) {
        set(defaultPath +path, def);
        return Config.config.node(splitPath(configPath+path)).getString(
                Config.config.node(splitPath(defaultPath +path)).getString(def));
    }

    /** DO NOT EDIT ANYTHING ABOVE **/

    public boolean GLOBALCHAT = false;
    public boolean JOINLEAVEMSSAGES = true;
    public boolean MUTED = false;
    public boolean FIRST_JOIN_MESSAGES = false;
    private void ServerSettings() {
        GLOBALCHAT = getBoolean("global-chat-enabled", GLOBALCHAT);
        JOINLEAVEMSSAGES = getBoolean("joinleave-messages-enabled", JOINLEAVEMSSAGES);
        MUTED = getBoolean("server-muted", MUTED);
        FIRST_JOIN_MESSAGES = getBoolean("first-join-messages", FIRST_JOIN_MESSAGES);
    }
}
