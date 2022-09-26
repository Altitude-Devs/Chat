package com.alttd.chat.util;

import com.alttd.chat.ChatAPI;
import com.alttd.chat.config.Config;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import org.bukkit.permissions.Permission;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utility {

    private static final List<String> EMPTY_LIST = new ArrayList<>();
    static final Pattern DEFAULT_URL_PATTERN = Pattern.compile("(?:(https?)://)?([-\\w_.]+\\.\\w{2,})(/\\S*)?");

    private static MiniMessage miniMessage = null;

    public static String stringRegen = "\\{#[A-Fa-f0-9]{6}(<)?(>)?}";
    public static HashMap<String, String> colors;
    private static LegacyComponentSerializer legacySerializer;
    static { // this might be in minimessage already?
        colors = new HashMap<>();
        colors.put("&0", "<black>");
        colors.put("&1", "<dark_blue>");
        colors.put("&2", "<dark_green>");
        colors.put("&3", "<dark_aqua>");
        colors.put("&4", "<dark_red>");
        colors.put("&5", "<dark_purple>");
        colors.put("&6", "<gold>");
        colors.put("&7", "<gray>");
        colors.put("&8", "<dark_gray>");
        colors.put("&9", "<blue>");
        colors.put("&a", "<green>");
        colors.put("&b", "<aqua>");
        colors.put("&c", "<red>");
        colors.put("&d", "<light_purple>");
        colors.put("&e", "<yellow>");
        colors.put("&f", "<white>");
    }
    
    public static HashMap<String, Pair<TagResolver, List<String>>> formattingPerms = new HashMap<>();
    static {
        formattingPerms.put("chat.format.color",
                new Pair<>(StandardTags.color(), colors.values().stream().toList()));
        formattingPerms.put("chat.format.bold",
                new Pair<>(StandardTags.decorations(TextDecoration.BOLD), List.of("<bold>", "<b>")));
        formattingPerms.put("chat.format.italic",
                new Pair<>(StandardTags.decorations(TextDecoration.ITALIC), List.of("<italic>", "<i>")));
        formattingPerms.put("chat.format.underlined",
                new Pair<>(StandardTags.decorations(TextDecoration.UNDERLINED), List.of("<underlined>", "<u>")));
        formattingPerms.put("chat.format.strikethrough",
                new Pair<>(StandardTags.decorations(TextDecoration.STRIKETHROUGH), List.of("<strikethrough>", "<st>")));
        formattingPerms.put("chat.format.obfuscated",
                new Pair<>(StandardTags.decorations(TextDecoration.OBFUSCATED), List.of("<obfuscated>", "<obf>")));
        formattingPerms.put("chat.format.gradient",
                new Pair<>(StandardTags.gradient(), EMPTY_LIST));
        formattingPerms.put("chat.format.font",
                new Pair<>(StandardTags.font(), EMPTY_LIST));
        formattingPerms.put("chat.format.rainbow",
                new Pair<>(StandardTags.rainbow(), List.of("<rainbow>")));
        formattingPerms.put("chat.format.hover",
                new Pair<>(StandardTags.hoverEvent(), EMPTY_LIST));
        formattingPerms.put("chat.format.click",
                new Pair<>(StandardTags.clickEvent(), EMPTY_LIST));
        formattingPerms.put("chat.format.transition",
                new Pair<>(StandardTags.transition(), EMPTY_LIST));
        formattingPerms.put("chat.format.reset",
                new Pair<>(StandardTags.reset(), List.of("<reset>", "<r>")));
    }

    public static String parseColors(String message) {
        if (message == null) return "";
        // split string in sections and check those vs looping hashmap?:/
        // think this is better, but will check numbers on this
        for (String key : colors.keySet()) {
            if (message.contains(key)) {
                message = message.replace(key, colors.get(key));
            }
        }
        return message;
    }

    public static Component getPrefix(UUID uuid, boolean single) {
        StringBuilder prefix = new StringBuilder();
        LuckPerms luckPerms = ChatAPI.get().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        List<String> prefixGroups = Config.PREFIXGROUPS;
        if(user == null) return Component.empty();
        if(!single) {
            Collection<Group> inheritedGroups = user.getInheritedGroups(user.getQueryOptions());
            if(inheritedGroups.stream().map(Group::getName).collect(Collectors.toList()).contains("eventleader")) {
                prefixGroups.remove("eventteam"); // hardcoded for now, new prefix system would load this from config
            }
            inheritedGroups.stream()
                    .sorted(Comparator.comparingInt(o -> o.getWeight().orElse(0)))
                    .forEach(group -> {
                        if (prefixGroups.contains(group.getName())) {
                            prefix.append(getGroupPrefix(group));
                        }
                    });
        }
        prefix.append(getUserPrefix(user));
//        prefix.append(user.getCachedData().getMetaData().getPrefix());

        return applyColor(prefix.toString());
    }

    public static Component getStaffPrefix(UUID uuid) {
        StringBuilder prefix = new StringBuilder();
        LuckPerms luckPerms = ChatAPI.get().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        if(user == null) return Component.empty();
        if(user.getCachedData().getPermissionData().checkPermission("group." + Config.MINIMIUMSTAFFRANK).asBoolean()) {
            Group group = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
            if(group != null)
                prefix.append(getGroupPrefix(group));
//                prefix.append(group.getCachedData().getMetaData().getPrefix());
        }
        return applyColor(prefix.toString());
    }

    public static String getUserPrefix(User user) {
        LuckPerms luckPerms = ChatAPI.get().getLuckPerms();
        Group group = luckPerms.getGroupManager().getGroup(user.getPrimaryGroup());
        if (group == null) {
            return "";
        }
        return ChatAPI.get().getPrefixes().get(group.getName()).replace("<prefix>", user.getCachedData().getMetaData().getPrefix());
    }

    public static String getGroupPrefix(String groupName) {
        Group group = ChatAPI.get().getLuckPerms().getGroupManager().getGroup(groupName);
        if (group == null) {
            return "";
        }
        return getGroupPrefix(group);
    }

    public static String getGroupPrefix(Group group) {
        return ChatAPI.get().getPrefixes().get(group.getName()).replace("<prefix>", group.getCachedData().getMetaData().getPrefix());
    }

    public static String getDisplayName(UUID uuid, String playerName) {
        if (!playerName.isBlank()) return playerName;
        LuckPerms luckPerms = ChatAPI.get().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        if(user == null) return "";
        return user.getUsername();
    }

    public static void flipPermission(UUID uuid, String permission) {
        ChatAPI.get().getLuckPerms().getUserManager().modifyUser(uuid, user -> {
            // Add the permission
            user.data().add(Node.builder(permission)
                    .value(!user.getCachedData().getPermissionData().checkPermission(permission).asBoolean()).build());
        });
    }

    public static boolean hasPermission(UUID uuid, String permission) {
        LuckPerms luckPerms = ChatAPI.get().getLuckPerms();
        User user = luckPerms.getUserManager().getUser(uuid);
        if(user == null) return false;
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public static Component applyColor(String message) {
        String hexColor1 = "";
        String hexColor2 = "";
        StringBuilder stringBuilder = new StringBuilder();
        message = parseColors(message);
        boolean startsWithColor = false;
        boolean lastColorMatters = false;

        if (message.matches(".*" + stringRegen + ".*")) {
            String[] split = message.split(stringRegen);

            ArrayList<String> list = new ArrayList<>();
            int nextIndex = 0;
            if (message.indexOf("}") <= 11) {
                startsWithColor = true;
                list.add(message.substring(0, message.indexOf("}") + 1));
            }
            for (String s : split) {
                nextIndex += s.length();
                int tmp = message.indexOf("}", nextIndex);
                if (tmp < message.length() && tmp>=0) {
                    list.add(message.substring(nextIndex, tmp + 1));
                    nextIndex = tmp + 1;
                }
            }

            int i;
            boolean firstLoop = true;
            if (startsWithColor) {
                i = -1;
            } else {
                i = 0;
                stringBuilder.append(split[i]);
            }

            for (String s : list) {
                boolean lesser = s.contains("<");
                boolean bigger = s.contains(">");

                if (bigger && lesser) {
                    hexColor2 = s.substring(1, s.length() - 3);
                } else if (bigger || lesser) {
                    hexColor2 = s.substring(1, s.length() - 2);
                } else {
                    hexColor2 = s.substring(1, s.length() -1);
                }

                if (firstLoop) {
                    lastColorMatters = bigger;
                    hexColor1 = hexColor2;
                    firstLoop = false;
                    i++;
                    continue;
                }

                if (lesser && lastColorMatters) {
                    stringBuilder.append("<gradient:").append(hexColor1).append(":").append(hexColor2).append(">").append(split[i]).append("</gradient>");
                } else {
                    stringBuilder.append("<").append(hexColor1).append(">").append(split[i]);
                }

                hexColor1 = hexColor2;
                lastColorMatters = bigger;
                i++;
            }
            if (split.length > i){
                stringBuilder.append("<").append(hexColor1).append(">").append(split[i]);
            }
        }
        return stringBuilder.length() == 0 ? Utility.parseMiniMessage(message)
                : Utility.parseMiniMessage(stringBuilder.toString());
    }

    public static String formatText(String message) {
            /*
                .match(pattern)
                .replacement(url -> {
                  String clickUrl = url.content();
                  if (!URL_SCHEME_PATTERN.matcher(clickUrl).find()) {
                    clickUrl = "http://" + clickUrl;
                  }
                  return (style == null ? url : url.style(style)).clickEvent(ClickEvent.openUrl(clickUrl));
                })
                .build();
              */
        Matcher matcher = DEFAULT_URL_PATTERN.matcher(message);
        while (matcher.find()) {
            String url = matcher.group();
            String clickUrl = url;
            String urlFormat = Config.URLFORMAT;
            message = message.replace(url, urlFormat.replaceAll("<url>", url).replaceAll("<clickurl>", clickUrl));
        }
        return message;
    }

    public static Component parseMiniMessage(String message) {
        return getMiniMessage().deserialize(message);
    }

    public static Component parseMiniMessage(String message, TagResolver placeholders) {
        if (placeholders == null) {
            return getMiniMessage().deserialize(message);
        } else {
            return getMiniMessage().deserialize(message, placeholders);
        }
    }

    public static Component parseMiniMessage(String message, TagResolver ... placeholders) {
        if (placeholders == null) {
            return getMiniMessage().deserialize(message);
        } else {
            return getMiniMessage().deserialize(message, TagResolver.resolver(placeholders));
        }
    }

    public static String stripTokens(String input) {
        return getMiniMessage().stripTags(input);
    }

    public static MiniMessage getMiniMessage() {
        if (miniMessage == null) miniMessage = MiniMessage.miniMessage();
        return miniMessage;
    }

    public static String removeAllColors(String string) {

        for (String colorCodes : colors.keySet()) {
            string = string.replace(colorCodes, "");
        }

        return string.replaceAll("\\{#[A-Fa-f0-9]{6}(<)?(>)?}", "");
    }

}
