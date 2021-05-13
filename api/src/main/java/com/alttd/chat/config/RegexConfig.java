package com.alttd.chat.config;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.util.regex.Pattern;

public final class RegexConfig {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");

    private final String regexName;
    private final String configPath;

    public RegexConfig(String regexName) {
        this.regexName = regexName;
        this.configPath = "regex-settings." + this.regexName + ".";
        init();
    }

    public void init() {
        Config.readConfig(RegexConfig.class, this);
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
        set(configPath + path, def);
        return Config.config.getNode(splitPath(configPath+path)).getBoolean(def);
    }

    private double getDouble(String path, double def) {
        set(configPath +path, def);
        return Config.config.getNode(splitPath(configPath+path)).getDouble(def);
    }

    private int getInt(String path, int def) {
        set(configPath +path, def);
        return Config.config.getNode(splitPath(configPath+path)).getInt(def);
    }

    private String getString(String path, String def) {
        set(configPath +path, def);
        return Config.config.getNode(splitPath(configPath+path)).getString(def);
    }

    /** DO NOT EDIT ANYTHING ABOVE **/

    public String REGEX = "REGEX";
    public String TYPE = "TYPE";
    public String REPLACEMENT = "REPLACEMENT";
    private void ServerSettings() {
        REGEX = getString("regex", REGEX);
        TYPE = getString("type", TYPE);
        REPLACEMENT = getString("replacement", REPLACEMENT);
    }
}
