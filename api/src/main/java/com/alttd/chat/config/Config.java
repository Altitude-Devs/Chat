package com.alttd.chat.config;

import com.alttd.chat.objects.channels.CustomChannel;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import io.leangen.geantyref.TypeToken;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.yaml.NodeStyle;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import org.spongepowered.configurate.ConfigurationOptions;

public final class Config {
    private static final Pattern PATH_PATTERN = Pattern.compile("\\.");
    private static final String HEADER = "";

    private static File CONFIG_FILE;
    public static ConfigurationNode config;
    public static YamlConfigurationLoader configLoader;

    static int version;
    static boolean verbose;

    public static File CONFIGPATH;
    public static void init() {
        CONFIGPATH = new File(System.getProperty("user.home") + File.separator + "share" + File.separator + "configs" + File.separator + "ChatPlugin");
        CONFIG_FILE = new File(CONFIGPATH, "config.yml");
        configLoader = YamlConfigurationLoader.builder()
                .file(CONFIG_FILE)
                .nodeStyle(NodeStyle.BLOCK)
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
            config = configLoader.load(ConfigurationOptions.defaults().header(HEADER).shouldCopyDefaults(false));
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
        if(config.node(splitPath(path)).virtual()) {
            try {
                config.node(splitPath(path)).set(def);
            } catch (SerializationException e) {
            }
        }
    }

    private static void setString(String path, String def) {
        try {
            if(config.node(splitPath(path)).virtual())
                config.node(splitPath(path)).set(io.leangen.geantyref.TypeToken.get(String.class), def);
        } catch(SerializationException ex) {
        }
    }

    private static boolean getBoolean(String path, boolean def) {
        set(path, def);
        return config.node(splitPath(path)).getBoolean(def);
    }

    private static double getDouble(String path, double def) {
        set(path, def);
        return config.node(splitPath(path)).getDouble(def);
    }

    private static int getInt(String path, int def) {
        set(path, def);
        return config.node(splitPath(path)).getInt(def);
    }

    private static String getString(String path, String def) {
        setString(path, def);
        return config.node(splitPath(path)).getString(def);
    }

    private static Long getLong(String path, Long def) {
        set(path, def);
        return config.node(splitPath(path)).getLong(def);
    }

    private static <T> List<String> getList(String path, T def) {
        try {
            set(path, def);
            return config.node(splitPath(path)).getList(TypeToken.get(String.class));
        } catch(SerializationException ex) {
        }
        return new ArrayList<>();
    }

    private static ConfigurationNode getNode(String path) {
        if(config.node(splitPath(path)).virtual()) {
            //new RegexConfig("Dummy");
        }
        config.childrenMap();
        return config.node(splitPath(path));
    }

    /** ONLY EDIT ANYTHING BELOW THIS LINE **/
    public static List<String> PREFIXGROUPS = new ArrayList<>();
    public static List<String> CONFLICTINGPREFIXGROUPS = new ArrayList<>();
    public static List<String> STAFFGROUPS = new ArrayList<>();
    public static String MINIMIUMSTAFFRANK = "trainee";
    public static String CONSOLENAME = "Console";
    public static UUID CONSOLEUUID = UUID.randomUUID();
    private static void settings() {
        PREFIXGROUPS = getList("settings.prefix-groups",
                Lists.newArrayList("discord", "socialmedia", "eventteam", "eventleader", "youtube", "twitch", "developer"));
        CONFLICTINGPREFIXGROUPS = getList("settings.prefix-conflicts-groups",
                Lists.newArrayList("eventteam", "eventleader"));
        STAFFGROUPS = getList("settings.staff-groups",
                Lists.newArrayList("trainee", "moderator", "headmod", "admin", "manager", "owner"));
        CONSOLENAME = getString("settings.console-name", CONSOLENAME);
        CONSOLEUUID = UUID.fromString(getString("settings.console-uuid", CONSOLEUUID.toString()));
        MINIMIUMSTAFFRANK = getString("settings.minimum-staff-rank", MINIMIUMSTAFFRANK);
    }

    public static List<String> MESSAGECOMMANDALIASES = new ArrayList<>();
    public static List<String> REPLYCOMMANDALIASES = new ArrayList<>();
    public static String MESSAGESENDER = "<hover:show_text:Click to reply><click:suggest_command:/msg <receivername> ><light_purple>(Me -> <gray><receiver></gray>)</hover> <message>";
    public static String MESSAGERECIEVER = "<hover:show_text:Click to reply><click:suggest_command:/msg <sendername> ><light_purple>(<gray><sender></gray> on <server> -> Me)</hover> <message>";
    public static String MESSAGESPY = "<gray>(<gray><sendername></gray> -> <receivername>) <message>";
    private static void messageCommand() {
        MESSAGECOMMANDALIASES.clear();
        REPLYCOMMANDALIASES.clear();
        MESSAGECOMMANDALIASES = getList("commands.message.aliases", Lists.newArrayList("msg", "whisper", "tell"));
        REPLYCOMMANDALIASES = getList("commands.reply.aliases", Lists.newArrayList("r"));
        MESSAGESENDER = getString("commands.message.sender-message", MESSAGESENDER);
        MESSAGERECIEVER = getString("commands.message.reciever-message", MESSAGERECIEVER);
        MESSAGESPY = getString("commands.message.spy-message", MESSAGESPY);
    }

    public static String GCFORMAT = "<white><light_purple><prefix></light_purple> <gray><sender></gray> <hover:show_text:on <server>><yellow>to Global</yellow></hover><gray>: <message>";
    public static String GCPERMISSION = "proxy.globalchat";
    public static List<String> GCALIAS = new ArrayList<>();
    public static String GCNOTENABLED = "You don't have global chat enabled.";
    public static String GCONCOOLDOWN = "You have to wait <cooldown> seconds before using this feature again.";
    public static int GCCOOLDOWN = 30;
    private static void globalChat() {
        GCFORMAT = getString("commands.globalchat.format", GCFORMAT);
        GCPERMISSION = getString("commands.globalchat.view-chat-permission", GCPERMISSION);
        GCALIAS.clear();
        GCALIAS = getList("commands.globalchat.alias", Lists.newArrayList("gc", "global"));
        GCNOTENABLED = getString("commands.globalchat.not-enabled", GCNOTENABLED);
        GCCOOLDOWN = getInt("commands.globalchat.cooldown", GCCOOLDOWN);
    }

    public static String CHATFORMAT = "<white><light_purple><prefixall> <gray><hover:show_text:Click to message <sendername>><click:suggest_command:/msg <sendername> ><sender></hover>: <white><message>";
    public static String URLFORMAT = "<click:OPEN_URL:<clickurl>><url></click>";
    private static void Chat() {
        CHATFORMAT = getString("chat.format", CHATFORMAT);
        URLFORMAT = getString("chat.urlformat", URLFORMAT);
    }

    public static List<String> GACECOMMANDALIASES = new ArrayList<>();
    public static String GACFORMAT = "<hover:show_text:Click to reply><click:suggest_command:/acg ><yellow>(<sender> on <server> -> Team)</hover> <message>";
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

    public static String PARTY_FORMAT = "<dark_aqua>(<gray><sender></gray> <hover:show_text:on <server>> → Party</hover>) <message>";
    public static String PARTY_SPY = "<i><gray>PC:</gray><dark_gray> <dark_gray><sendername></dark_gray>: <dark_gray><partyname></dark_gray> <message></dark_gray></i>";
    public static String PARTY_HELP = "";
    public static String NO_PERMISSION = "<red>You don't have permission to use this command.</red>";
    public static String NO_CONSOLE = "<red>This command can not be used by console</red>";
    public static String CREATED_PARTY = "<green>You created a chat party called: " +
            "'<gold><party_name></gold>' with the password: '<gold><party_password></gold>'</green>";
    public static String NOT_IN_A_PARTY = "<red>You're not in a chat party.</red>";
    public static String NOT_YOUR_PARTY = "<red>You don't own this chat party.</red>";
    public static String NOT_A_PARTY = "<red>This chat party does not exist.</red>";
    public static String INVALID_PLAYER = "<red>Invalid player.</red>";
    public static String NOT_ONLINE = "<red><player> must be online to receive an invite.</red>";
    public static String INVALID_PASSWORD = "<red>Invalid password.</red>";
    public static String JOINED_PARTY = "<green>You joined <party_name>!</green>";
    public static String NOTIFY_FINDING_NEW_OWNER = "<dark_aqua>Since you own this chat party a new party owner will be chosen.<dark_aqua>";
    public static String LEFT_PARTY = "<green>You have left the chat party!</green>";
    private static void party() {
        PARTY_FORMAT = getString("party.format", PARTY_FORMAT);
        PARTY_SPY = getString("party.spy", PARTY_SPY);
        PARTY_HELP = getString("party.messages.help", PARTY_HELP);
        NO_PERMISSION = getString("party.messages.no-permission", NO_PERMISSION);
        NO_CONSOLE = getString("party.messages.no-console", NO_CONSOLE);
        CREATED_PARTY = getString("party.messages.created-party", CREATED_PARTY);
        NOT_IN_A_PARTY = getString("party.messages.not-in-a-party", NOT_IN_A_PARTY);
        NOT_YOUR_PARTY = getString("party.messages.not-your-party", NOT_YOUR_PARTY);
        NOT_A_PARTY = getString("party.messages.not-a-party", NOT_A_PARTY);
        INVALID_PLAYER = getString("party.messages.invalid-player", INVALID_PLAYER);
        NOT_ONLINE = getString("party.messages.not-online", NOT_ONLINE);
        INVALID_PASSWORD = getString("party.messages.invalid-password", INVALID_PASSWORD);
        NOTIFY_FINDING_NEW_OWNER = getString("party.messages.notify-finding-new-owner", NOTIFY_FINDING_NEW_OWNER);
        LEFT_PARTY = getString("party.messages.left-party", LEFT_PARTY);
    }

    private static void chatChannels() {
        ConfigurationNode node = getNode("chat-channels");
        if (node.empty()) {
            getString("chat-channels.ac.format", "<white><gray><sender></gray> <hover:show_text:on <server>><yellow>to <channel></yellow></hover><gray>: <message>");
            getList("chat-channels.ac.servers", List.of("lobby"));
            getBoolean("chat-channels.ac.proxy", false);
            node = getNode("chat-channels");
        }

        for (ConfigurationNode configurationNode : node.childrenMap().values()) {
            String channelName = Objects.requireNonNull(configurationNode.key()).toString();
            String key = "chat-channels." + channelName + ".";
            new CustomChannel(channelName,
                    getString(key + "format", ""),
                    getList(key + "servers", Collections.EMPTY_LIST),
                    getBoolean(key + "proxy", false));
        }
    }

    public static String SERVERMUTEPERMISSION = "chat.command.mute-server";
    public static String SPYPERMISSION = "chat.socialspy";
    private static void permissions() {
        SERVERMUTEPERMISSION = getString("permissions.server-mute", SERVERMUTEPERMISSION);
        SPYPERMISSION = getString("permissions.server-mute", SPYPERMISSION);
    }

    public static String IP = "0.0.0.0";
    public static String PORT = "3306";
    public static String DATABASE = "database";
    public static String USERNAME = "root";
    public static String PASSWORD = "root";
    private static void database() {
        IP = getString("database.ip", IP);
        PORT = getString("database.port", PORT);
        DATABASE = getString("database.name", DATABASE);
        USERNAME = getString("database.username", USERNAME);
        PASSWORD = getString("database.password", PASSWORD);
    }

    public static String NOTIFICATIONFORMAT = "<red>[<prefix>] <displayname> <target> <input>";
    private static void notificationSettings() {
        NOTIFICATIONFORMAT = getString("settings.blockedmessage-notification", NOTIFICATIONFORMAT);
    }

    public static String mailHeader = "===== List Mails ====='";
    public static String mailBody = "<white>From:</white> <staffprefix><sender> <white>on:<date></white>\n<message>";
    public static String mailFooter = "======================";
    public static String mailNoUser = "<red>A player with this name hasn't logged in recently.";
    public static List<String> mailCommandAlias = new ArrayList<>();
    private static void mailSettings() {
        mailHeader = getString("settings.mail.header", mailHeader);
        mailBody = getString("settings.mail.message", mailBody);
        mailFooter = getString("settings.mail.footer", mailFooter);
        mailCommandAlias = getList("settings.mail.command-aliases", Lists.newArrayList("gmail"));
    }

}
