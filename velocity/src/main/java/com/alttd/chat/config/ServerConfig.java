package com.alttd.velocitychat.config;

import com.alttd.chat.config.Config;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

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
        if(Config.config.getNode(splitPath(path)).isVirtual()) {
            Config.config.getNode(splitPath(path)).setValue(def);
        }
    }

    private static void setString(String path, String def) {
        try {
            if(Config.config.getNode(splitPath(path)).isVirtual())
                Config.config.getNode(splitPath(path)).setValue(TypeToken.of(String.class), def);
        } catch(ObjectMappingException ex) {
        }
    }

    private boolean getBoolean(String path, boolean def) {
        set(defaultPath +path, def);
        return Config.config.getNode(splitPath(configPath+path)).getBoolean(
                Config.config.getNode(splitPath(defaultPath +path)).getBoolean(def));
    }

    private double getDouble(String path, double def) {
        set(defaultPath +path, def);
        return Config.config.getNode(splitPath(configPath+path)).getDouble(
                Config.config.getNode(splitPath(defaultPath +path)).getDouble(def));
    }

    private int getInt(String path, int def) {
        set(defaultPath +path, def);
        return Config.config.getNode(splitPath(configPath+path)).getInt(
                Config.config.getNode(splitPath(defaultPath +path)).getInt(def));
    }

    private String getString(String path, String def) {
        set(defaultPath +path, def);
        return Config.config.getNode(splitPath(configPath+path)).getString(
                Config.config.getNode(splitPath(defaultPath +path)).getString(def));
    }

    /** DO NOT EDIT ANYTHING ABOVE **/

}
