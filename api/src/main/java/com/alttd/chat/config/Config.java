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

    public static String PARTY_FORMAT = "<dark_aqua>(<gray><sender></gray><hover:show_text:\"on <server>\"> → <party></hover>) <message>";
    public static String PARTY_SPY = "<i><gray>PC:</gray><dark_gray> <dark_gray><sender></dark_gray>: <dark_gray><party></dark_gray> <message></dark_gray></i>";
    public static String NO_PERMISSION = "<red>You don't have permission to use this command.</red>";
    public static String NO_CONSOLE = "<red>This command can not be used by console</red>";
    public static String CREATED_PARTY = "<green>You created a chat party called: " +
            "'<gold><party_name></gold>' with the password: '<gold><party_password></gold>'</green>";
    public static String NOT_IN_A_PARTY = "<red>You're not in a chat party.</red>";
    public static String NOT_YOUR_PARTY = "<red>You don't own this chat party.</red>";
    public static String NOT_A_PARTY = "<red>This chat party does not exist.</red>";
    public static String PARTY_EXISTS = "<red>A chat party called <party> already exists.</red>";
    public static String INVALID_PLAYER = "<red>Invalid player.</red>";
    public static String NOT_ONLINE = "<red><player> must be online to receive an invite.</red>";
    public static String INVALID_PASSWORD = "<red>Invalid password.</red>";
    public static String JOINED_PARTY = "<green>You joined <party_name>!</green>";
    public static String NOTIFY_FINDING_NEW_OWNER = "<dark_aqua>Since you own this chat party a new party owner will be chosen.<dark_aqua>";
    public static String LEFT_PARTY = "<green>You have left the chat party!</green>";
    public static String OWNER_LEFT_PARTY = "<dark_aqua>[ChatParty]: <old_owner> left the chat party, the new party owner is <new_owner>";
    public static String NEW_PARTY_OWNER = "<dark_aqua>[ChatParty]: <old_owner> transferred the party to <new_owner>!";
    public static String CANT_REMOVE_PARTY_OWNER = "<red>You can't remove yourself, please leave instead.</red>";
    public static String REMOVED_FROM_PARTY = "<red>You were removed from the '<party>' chat party.</red>";
    public static String REMOVED_USER_FROM_PARTY = "<green>You removed <player> from the chat party!</green>";
    public static String NOT_A_PARTY_MEMBER = "<red><player> is not a member of your party!</red>";
    public static String ALREADY_IN_PARTY = "<red>You're already in a party!</red>";
    public static String SENT_PARTY_INV = "<green>You send a chat party invite to <player>!</green>";
    public static String JOIN_PARTY_CLICK_MESSAGE = "<click:run_command:'/party join <party> <party_password>'>" +
            "<dark_aqua>You received an invite to join <party> click this message to accept.</dark_aqua></click>";
    public static String PARTY_MEMBER_LOGGED_ON = "<dark_aqua>[ChatParty] <player> joined Altitude...</dark_aqua>";
    public static String PARTY_MEMBER_LOGGED_OFF = "<dark_aqua>[ChatParty] <player> left Altitude...</dark_aqua>";
    public static String RENAMED_PARTY = "<dark_aqua>[ChatParty] <owner> changed the party name from <old_name> to <new_name>!</dark_aqua>";
    public static String CHANGED_PASSWORD = "<green>Password was set to <password></green>";
    public static String DISBAND_PARTY_CONFIRM = "<green><bold>Are you sure you want to disband your party?</bold> " +
            "Type <gold>/party disband confirm <party></gold> to confirm.";
    public static String DISBANDED_PARTY = "<dark_aqua>[ChatParty] <owner> has disbanded <party>, everyone has been removed.</dark_aqua>";
    public static String PARTY_INFO = """
                <gold><bold>Chat party info</bold>:
                </gold><green>Name: <dark_aqua><party></dark_aqua>
                Password: <dark_aqua><password></dark_aqua>
                Owner: <owner>
                Members: <members>""";
    private static void party() {
        PARTY_FORMAT = getString("party.format", PARTY_FORMAT);
        PARTY_SPY = getString("party.spy", PARTY_SPY);
        NO_PERMISSION = getString("party.messages.no-permission", NO_PERMISSION);
        NO_CONSOLE = getString("party.messages.no-console", NO_CONSOLE);
        CREATED_PARTY = getString("party.messages.created-party", CREATED_PARTY);
        NOT_IN_A_PARTY = getString("party.messages.not-in-a-party", NOT_IN_A_PARTY);
        NOT_YOUR_PARTY = getString("party.messages.not-your-party", NOT_YOUR_PARTY);
        NOT_A_PARTY = getString("party.messages.not-a-party", NOT_A_PARTY);
        INVALID_PLAYER = getString("party.messages.invalid-player", INVALID_PLAYER);
        NOT_ONLINE = getString("party.messages.not-online", NOT_ONLINE);
        INVALID_PASSWORD = getString("party.messages.invalid-password", INVALID_PASSWORD);
        JOINED_PARTY = getString("party.messages.joined-party", JOINED_PARTY);
        NOTIFY_FINDING_NEW_OWNER = getString("party.messages.notify-finding-new-owner", NOTIFY_FINDING_NEW_OWNER);
        LEFT_PARTY = getString("party.messages.left-party", LEFT_PARTY);
        OWNER_LEFT_PARTY = getString("party.messages.owner-left-party", OWNER_LEFT_PARTY);
        NEW_PARTY_OWNER = getString("party.messages.new-owner", NEW_PARTY_OWNER);
        CANT_REMOVE_PARTY_OWNER = getString("party.messages.cant-remove-owner", CANT_REMOVE_PARTY_OWNER);
        REMOVED_FROM_PARTY = getString("party.messages.removed-from-party", REMOVED_FROM_PARTY);
        NOT_A_PARTY_MEMBER = getString("party.messages.not-a-party-member", NOT_A_PARTY_MEMBER);
        JOIN_PARTY_CLICK_MESSAGE = getString("party.messages.join-party-click-message", JOIN_PARTY_CLICK_MESSAGE);
        SENT_PARTY_INV = getString("party.messages.sent-party-invite", SENT_PARTY_INV);
        PARTY_MEMBER_LOGGED_ON = getString("party.messages.party-member-logged-on", PARTY_MEMBER_LOGGED_ON);
        PARTY_MEMBER_LOGGED_OFF = getString("party.messages.party-member-logged-off", PARTY_MEMBER_LOGGED_OFF);
        RENAMED_PARTY = getString("party.messages.renamed-party", RENAMED_PARTY);
        CHANGED_PASSWORD = getString("party.messages.changed-password", CHANGED_PASSWORD);
        DISBAND_PARTY_CONFIRM = getString("party.messages.disband-party-confirm", DISBAND_PARTY_CONFIRM);
        DISBANDED_PARTY = getString("party.messages.disbanded-party", DISBANDED_PARTY);
        PARTY_INFO = getString("party.messages.party-info", PARTY_INFO);
    }

    public static String PARTY_HELP_WRAPPER = "<gold>ChatParty help:\n<commands></gold>";
    public static String PARTY_HELP_HELP = "<green>Show this menu: <gold>/party help</gold></green>";
    public static String PARTY_HELP_CREATE = "<green>Create a party: <gold>/party create <party_name> <party_password></gold></green>";
    public static String PARTY_HELP_INFO = "<green>Show info about your current party: <gold>/party info</gold></green>";
    public static String PARTY_HELP_INVITE = "<green>Invite a user to your party: <gold>/party invite <username></gold></green>";
    public static String PARTY_HELP_JOIN = "<green>Join a party: <gold>/party join <party_name> <party_password></gold></green>";
    public static String PARTY_HELP_LEAVE = "<green>Leave your current party: <gold>/party leave</gold></green>";
    public static String PARTY_HELP_NAME = "<green>Change the name of your party: <gold>/party name <new_name></gold></green>";
    public static String PARTY_HELP_OWNER = "<green>Change the owner of your party: <gold>/party owner <new_owner_name></gold></green>";
    public static String PARTY_HELP_PASSWORD = "<green>Change the password of your party: <gold>/party password <new_password></gold></green>";
    public static String PARTY_HELP_REMOVE = "<green>Remove a member from your party: <gold>/party remove <member_name></gold></green>";
    public static String PARTY_HELP_DISBAND = "<green>Remove everyone from your party and disband it: <gold>/party disband</gold></green>";
    public static String PARTY_HELP_CHAT = "<green>Talk in party chat: <gold>/p <message></gold></green>";
    private static void partyHelp() {
        PARTY_HELP_WRAPPER = getString("party.help.wrapper", PARTY_HELP_WRAPPER);
        PARTY_HELP_HELP = getString("party.help.help", PARTY_HELP_HELP);
        PARTY_HELP_CREATE = getString("party.help.create", PARTY_HELP_CREATE);
        PARTY_HELP_INFO = getString("party.help.info", PARTY_HELP_INFO);
        PARTY_HELP_INVITE = getString("party.help.invite", PARTY_HELP_INVITE);
        PARTY_HELP_JOIN = getString("party.help.join", PARTY_HELP_JOIN);
        PARTY_HELP_LEAVE = getString("party.help.leave", PARTY_HELP_LEAVE);
        PARTY_HELP_NAME = getString("party.help.name", PARTY_HELP_NAME);
        PARTY_HELP_OWNER = getString("party.help.owner", PARTY_HELP_OWNER);
        PARTY_HELP_PASSWORD = getString("party.help.password", PARTY_HELP_PASSWORD);
        PARTY_HELP_REMOVE = getString("party.help.remove", PARTY_HELP_REMOVE);
        PARTY_HELP_DISBAND = getString("party.help.disband", PARTY_HELP_DISBAND);
        PARTY_HELP_CHAT = getString("party.help.chat", PARTY_HELP_CHAT);
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
