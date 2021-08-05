package com.alttd.chat.config;

import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.objects.ChatFilter;
import com.alttd.chat.util.ALogger;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public final class RegexConfig {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");
    private static final String HEADER =
            "This file is used to store all of the chatfilters used in the Altitude Chat plugin.\n"
            + "Nodes must be build in this format.\n"
            + "TODO update this format\n";

    private static File CONFIG_FILE;
    public static ConfigurationNode config;
    public static YAMLConfigurationLoader configLoader;

    public static void init() {
        CONFIG_FILE = new File(Config.CONFIGPATH, "filters.yml");;
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

        readConfig(RegexConfig.class, null);
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

    private static Object[] splitPath(String key) {
        return PATH_PATTERN.split(key);
    }

    private static void set(String path, Object def) {
        if(config.getNode(splitPath(path)).isVirtual())
            config.getNode(splitPath(path)).setValue(def);
    }

    private static void setString(String path, String def) {
        try {
            if(config.getNode(splitPath(path)).isVirtual())
                config.getNode(splitPath(path)).setValue(TypeToken.of(String.class), def);
        } catch(ObjectMappingException ex) {
        }
    }

    private static boolean getBoolean(String path, boolean def) {
        set(path, def);
        return config.getNode(splitPath(path)).getBoolean(def);
    }

    private static double getDouble(String path, double def) {
        set(path, def);
        return config.getNode(splitPath(path)).getDouble(def);
    }

    private static int getInt(String path, int def) {
        set(path, def);
        return config.getNode(splitPath(path)).getInt(def);
    }

    private static String getString(String path, String def) {
        setString(path, def);
        return config.getNode(splitPath(path)).getString(def);
    }

    private static Long getLong(String path, Long def) {
        set(path, def);
        return config.getNode(splitPath(path)).getLong(def);
    }

    private static <T> List<String> getList(String path, T def) {
        try {
            set(path, def);
            return config.getNode(splitPath(path)).getList(TypeToken.of(String.class));
        } catch(ObjectMappingException ex) {
        }
        return new ArrayList<>();
    }

    public static List<String> ChatFilters = new ArrayList<>();
    private static void loadChatFilters() {
        ALogger.info("loading filters");
//        for (Map.Entry<Object, ? extends ConfigurationNode> entry : config.getChildrenMap().entrySet()) {
//            String name = entry.getKey().toString(); // the name in the config this filter has
//            String type = entry.getValue().getNode("type").getString(); // the type of filter, block or replace
//            String regex = "";
//            List<String> replacements = new ArrayList<>();
//            List<String> exclusions = new ArrayList<>();
//            Map<Object, ? extends ConfigurationNode> options = entry.getValue().getNode("options").getChildrenMap();
//            if (options.containsKey("filter")) {
//                regex = options.get("filter").getString();
//            }
//            if (options.containsKey("replacements")) {
//                options.get("replacements").getChildrenList().forEach(key -> {
//                    replacements.add(key.getString());
//                });
//            }
//            if (options.containsKey("exclusions")) {
//                options.get("exclusions").getChildrenList().forEach(key -> {
//                    exclusions.add(key.getString());
//                });
//            }
//        }

        Map<String, Object> properties = new HashMap<>();
        config.getChildrenMap().entrySet().forEach(entry -> {
            try {
                String name = entry.getKey().toString();
                String type = entry.getValue().getNode("type").getString();
                String regex = entry.getValue().getNode("regex").getString();
                String replacement = entry.getValue().getNode("replacement").getString();
                List<String> exclusions = entry.getValue().getNode("exclusions").getList(TypeToken.of(String.class), new ArrayList<>());
                if (type == null || type.isEmpty() || regex == null || regex.isEmpty()) {
                    ALogger.warn("Filter: " + name + " was set up incorrectly");
                } else {
                    if (replacement == null || replacement.isEmpty()) {
                        replacement = name;
                    }
                    ChatFilter chatFilter = new ChatFilter(name, type, regex, replacement, exclusions);
                    RegexManager.addFilter(chatFilter);
                }
            } catch(ObjectMappingException ex) {
            }
        });

    }
}
