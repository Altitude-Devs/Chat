package com.alttd.chat.config;

import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.regex.Pattern;

public final class PrefixConfig {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");
    private static final String HEADER =
            "This file is used to store all of the prefix settings used in the Altitude Chat plugin.\n"
                    + "Legacy and MiniMessage formatting can be applied to these settings.\n";

    private static File CONFIG_FILE;
    public static ConfigurationNode config;
    public static YAMLConfigurationLoader configLoader;

    private static String prefixName;
    private static String configPath;
    private static String defaultPath;

    public PrefixConfig(String prefix) {
        prefixName = prefix;
        configPath = "prefix-settings." + prefixName + ".";
        defaultPath = "prefix-settings.default.";
        init();
    }

    public static File CONFIGPATH;
    public void init() {
        CONFIGPATH = new File(System.getProperty("user.home") + File.separator + "share" + File.separator + "ChatPlugin");
        CONFIG_FILE = new File(CONFIGPATH, "prefix.yml");
        configLoader = YAMLConfigurationLoader.builder()
                .setFile(CONFIG_FILE)
                .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
                .build();
        if (!CONFIG_FILE.getParentFile().exists()) {
            if(!CONFIG_FILE.getParentFile().mkdirs()) {
                return;
            }
        }
        if (!CONFIG_FILE.exists()) {
            try {
                if(!CONFIG_FILE.createNewFile()) {
                    return;
                }
            } catch (IOException error) {
                error.printStackTrace();
            }
        }

        try {
            config = configLoader.load(ConfigurationOptions.defaults().setHeader(HEADER));
        } catch (IOException e) {
            e.printStackTrace();
        }

        readConfig(PrefixConfig.class, null);
        try {
            configLoader.save(config);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readConfig(Class<?> clazz, Object instance) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (Modifier.isPrivate(method.getModifiers())) {
                if (method.getParameterTypes().length == 0 && method.getReturnType() == Void.TYPE) {
                    try {
                        method.setAccessible(true);
                        method.invoke(instance);
                    } catch (InvocationTargetException | IllegalAccessException ex) {
                        throw Throwables.propagate(ex.getCause());
                    }
                }
            }
        }
        try {
            configLoader.save(config);
        } catch (IOException ex) {
            throw Throwables.propagate(ex.getCause());
        }
    }

    public static void saveConfig() {
        try {
            configLoader.save(config);
        } catch (IOException ex) {
            throw Throwables.propagate(ex.getCause());
        }
    }

    public static Object[] splitPath(String key) {
        return PATH_PATTERN.split(key);
    }

    private static void set(String path, Object def) {
        if(config.getNode(splitPath(path)).isVirtual()) {
            config.getNode(splitPath(path)).setValue(def);
        }
    }

    private static void setString(String path, String def) {
        try {
            if(config.getNode(splitPath(path)).isVirtual())
                config.getNode(splitPath(path)).setValue(TypeToken.of(String.class), def);
        } catch(ObjectMappingException ex) {
        }
    }

    private static boolean getBoolean(String path, boolean def) {
        set(defaultPath + path, def);
        return config.getNode(splitPath(configPath + path)).getBoolean(
                config.getNode(splitPath(defaultPath + path)).getBoolean(def));
    }

    private static double getDouble(String path, double def) {
        set(defaultPath + path, def);
        return config.getNode(splitPath(configPath + path)).getDouble(
                config.getNode(splitPath(defaultPath + path)).getDouble(def));
    }

    private static int getInt(String path, int def) {
        set(defaultPath + path, def);
        return config.getNode(splitPath(configPath + path)).getInt(
                config.getNode(splitPath(defaultPath + path)).getInt(def));
    }

    private static String getString(String path, String def) {
        set(defaultPath + path, def);
        return config.getNode(splitPath(configPath + path)).getString(
                config.getNode(splitPath(defaultPath + path)).getString(def));
    }

    /** ONLY EDIT ANYTHING BELOW THIS LINE **/
    public static String PREFIXFORMAT = "<prefix>";
    private static void PrefixSettings() {
        PREFIXFORMAT = getString("format", PREFIXFORMAT);
    }

}
