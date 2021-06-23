package com.alttd.chat.config;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public final class Config {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");
    private static final String HEADER = "";

    private static File CONFIG_FILE;
    public static ConfigurationNode config;
    public static YAMLConfigurationLoader configLoader;

    static int version;
    static boolean verbose;

    public static File CONFIGPATH;
    public static void init() { // todo setup share for the config
        CONFIGPATH = new File(System.getProperty("user.home")+File.separator+"ChatPlugin");
        CONFIG_FILE = new File(CONFIGPATH, "config.yml");;
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

    private static ConfigurationNode getNode(String path) {
        if(config.getNode(splitPath(path)).isVirtual()) {
            //new RegexConfig("Dummy");
        }
        config.getChildrenMap();
        return config.getNode(splitPath(path));
    }

    /** ONLY EDIT ANYTHING BELOW THIS LINE **/
    public static List<String> PREFIXGROUPS = new ArrayList<>();
    public static List<String> STAFFGROUPS = new ArrayList<>();
    public static String MINIMIUMSTAFFRANK = "trainee";
    public static String CONSOLENAME = "Console";
    public static UUID CONSOLEUUID = UUID.randomUUID();
    private static void settings() {
        PREFIXGROUPS = getList("settings.prefix-groups",
                Lists.newArrayList("discord", "socialmedia", "eventteam", "eventleader", "youtube", "twitch", "developer"));
        STAFFGROUPS = getList("settings.staff-groups",
                Lists.newArrayList("trainee", "moderator", "headmod", "admin", "manager", "owner"));
        CONSOLENAME = getString("settings.console-name", CONSOLENAME);
        CONSOLEUUID = UUID.fromString(getString("settings.console-uuid", CONSOLEUUID.toString()));
        MINIMIUMSTAFFRANK = getString("settings.minimum-staff-rank", MINIMIUMSTAFFRANK);
    }

    public static List<String> MESSAGECOMMANDALIASES = new ArrayList<>();
    public static List<String> REPLYCOMMANDALIASES = new ArrayList<>();
    public static String MESSAGESENDER = "<hover:show_text:Click to reply><click:suggest_command:/msg <receiver> ><light_purple>(Me -> <gray><receiver></gray>) <message>";
    public static String MESSAGERECIEVER = "<hover:show_text:Click to reply><click:suggest_command:/msg <sender> ><light_purple>(<gray><sender></gray> on <server> -> Me) <message>";
    private static void messageCommand() {
        MESSAGECOMMANDALIASES.clear();
        REPLYCOMMANDALIASES.clear();
        MESSAGECOMMANDALIASES = getList("commands.message.aliases", Lists.newArrayList("msg", "whisper", "tell"));
        REPLYCOMMANDALIASES = getList("commands.reply.aliases", Lists.newArrayList("r"));
        MESSAGESENDER = getString("commands.message.sender-message", MESSAGESENDER);
        MESSAGERECIEVER = getString("commands.message.reciever-message", MESSAGERECIEVER);
    }
///broadcast <white><light_purple><prefix></light_purple> <gray>Momlly</gray> <hover:show_text:on Atoll><yellow>to Global</yellow></hover><gray>: We Love <gold>Teri</gold> and <light_purple>Kappa</light_purple></gray></white>
    public static String GCFORMAT = "<white><light_purple><prefix></light_purple> <gray><sender></gray> <hover:show_text:on <server>><yellow>to Global</yellow></hover><gray>: <message>";
    public static String GCPERMISSION = "proxy.globalchat";
    public static List<String> GCALIAS = new ArrayList<>();
    public static String GCNOTENABLED = "You don't have global chat enabled."; // todo mini message formatting
    public static String GCONCOOLDOWN = "You have to wait <cooldown> seconds before using this feature again."; // todo mini message formatting
    public static int GCCOOLDOWN = 30;
    private static void globalChat() {
        MESSAGERECIEVER = getString("commands.globalchat.format", MESSAGERECIEVER);
        GCPERMISSION = getString("commands.globalchat.view-chat-permission", GCPERMISSION);
        GCALIAS.clear();
        GCALIAS = getList("commands.globalchat.alias", Lists.newArrayList("gc", "global"));
        GCNOTENABLED = getString("commands.globalchat.not-enabled", GCNOTENABLED);
        GCCOOLDOWN = getInt("commands.globalchat.cooldown", GCCOOLDOWN);
    }

    // TODO prefixes need hovers, this hasn't been setup yet!
    public static String CHATFORMAT = "<white><light_purple><prefixall></light_purple> <gray><sender>: </gray><message>"; // @teri help with the default formatting?
    private static void Chat() {
        CHATFORMAT = getString("chat.format", CHATFORMAT);
    }

    public static List<String> GACECOMMANDALIASES = new ArrayList<>();
    public static String GACFORMAT = "<hover:show_text:Click to reply><click:suggest_command:/acg ><yellow>(<sender> on <server> -> Team) <message>";
    private static void globalAdminChat() {
        GACECOMMANDALIASES = getList("commands.globaladminchat.aliases", Lists.newArrayList("acg"));
        GACFORMAT = getString("commands.globaladminchat.format", GACFORMAT);
    }

    public static String MESSAGECHANNEL = "altitude:chatplugin";
    private static void messageChannels() {
        MESSAGECHANNEL = getString("settings.message-channel", MESSAGECHANNEL);
    }

    public static ConfigurationNode REGEXNODE = null;
    private static void RegexNOde() {
        REGEXNODE = getNode("regex-settings");
    }

    public static String SERVERSWTICHMESSAGEFROM = "<gray>* <player> comes from <from_server>...";
    public static String SERVERSWTICHMESSAGETO = "<gray>* <player> leaves to <to_server>...";
    public static String SERVERJOINMESSAGE = "<green>* <player> appears from thin air...";
    public static String SERVERLEAVEMESSAGE = "<red>* <player> vanishes in the mist...";
    private static void JoinLeaveMessages() {
        SERVERSWTICHMESSAGEFROM = getString("messages.switch-server-from", SERVERSWTICHMESSAGEFROM);
        SERVERSWTICHMESSAGETO = getString("messages.switch-server-to", SERVERSWTICHMESSAGETO);
        SERVERJOINMESSAGE = getString("messages.join-server", SERVERJOINMESSAGE);
        SERVERLEAVEMESSAGE = getString("messages.leave-server", SERVERLEAVEMESSAGE);

    }

    public static String DRIVER = "mysql";
    public static String IP = "0.0.0.0";
    public static String PORT = "3306";
    public static String DATABASE = "database";
    public static String USERNAME = "root";
    public static String PASSWORD = "root";
    private static void database() {
        DRIVER = getString("database.driver" , DRIVER);
        IP = getString("database.ip", IP);
        PORT = getString("database.port", PORT);
        DATABASE = getString("database.name", DATABASE);
        USERNAME = getString("database.username", USERNAME);
        PASSWORD = getString("database.password", PASSWORD);
    }

}
