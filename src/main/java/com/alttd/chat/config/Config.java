package com.alttd.chat.config;

import com.google.common.base.Throwables;
import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
import org.yaml.snakeyaml.DumperOptions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public final class Config {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");
    private static final String HEADER = "";

    private static File CONFIG_FILE;
    public static ConfigurationNode config;
    public static YAMLConfigurationLoader configLoader;

    static int version;
    static boolean verbose;

    public static void init(File path) {
        CONFIG_FILE = new File(path, "config.yml");;
        configLoader = YAMLConfigurationLoader.builder()
                .setFile(CONFIG_FILE)
                .setFlowStyle(DumperOptions.FlowStyle.BLOCK)
                .build();
        if (!CONFIG_FILE.getParentFile().exists()) {
            CONFIG_FILE.getParentFile().mkdirs();
        }
        if (!CONFIG_FILE.exists()) {
            try {
                CONFIG_FILE.createNewFile();
            } catch (IOException error) {
                error.printStackTrace();
            }
        }

        try {
            config = configLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }

        configLoader.getDefaultOptions().setHeader(HEADER);
        configLoader.getDefaultOptions().withShouldCopyDefaults(true);

        verbose = getBoolean("verbose", true);
        version = getInt("config-version", 1);

        readConfig(Config.class, null);
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
                    } catch (InvocationTargetException ex) {
                        throw Throwables.propagate(ex.getCause());
                    } catch (Exception ex) {
                    }
                }
            }
        }
        try {
            configLoader.save(config);
        } catch (IOException ex) {
        }
    }

    public static boolean saveConfig() {
        try {
            configLoader.save(config);
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    private static Object[] splitPath(String key) {
        return PATH_PATTERN.split(key);
    }

    private static void set(String path, Object def) {
        if(config.getNode(splitPath(path)).isVirtual())
            config.getNode(splitPath(path)).setValue(def);
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
        set(path, def);
        return config.getNode(splitPath(path)).getString(def);
    }

    private static Long getLong(String path, Long def) {
        set(path, def);
        return config.getNode(splitPath(path)).getLong(def);
    }

    private static <T> List getList(String path, T def) {
        try {
            set(path, def);
            return config.getNode(splitPath(path)).getList(TypeToken.of(String.class));
        } catch(ObjectMappingException ex) {
        }
        return new ArrayList<>();
    }

    /** ONLY EDIT ANYTHING BELOW THIS LINE **/

    public static List<String> MESSAGECOMMANDALIASES = new ArrayList<>();
    public static String MESSAGESENDER = "<hover:show_text:'Click to reply'><click:suggest_command:'/msg <receiver> '><light_purple>(Me -> <gray><receiver></gray>) <message></light_purple>";
    public static String MESSAGERECIEVER = "<hover:show_text:'Click to reply'><click:suggest_command:'/msg <receiver> '><light_purple>(<gray><receiver></gray> on <server> -> Me) <message></light_purple>";
    private static void messageCommand() {
        MESSAGECOMMANDALIASES.clear();
        getList("commands.message.aliases", new ArrayList<String>(){{
            add("msg");
            add("whisper");
            add("tell");
        }}).forEach(key -> {
            MESSAGECOMMANDALIASES.add(key.toString());
        });
        MESSAGESENDER = getString("commands.message.sender-message", MESSAGESENDER);
        MESSAGERECIEVER = getString("commands.message.reciever-message", MESSAGERECIEVER);
    }
}
