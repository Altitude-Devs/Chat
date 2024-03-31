package com.alttd.chat.managers;

import com.alttd.chat.ChatAPI;
import com.alttd.chat.config.RegexConfig;
import com.alttd.chat.objects.ChatFilter;
import com.alttd.chat.objects.FilterType;
import com.alttd.chat.objects.ModifiableString;
import com.alttd.chat.util.ALogger;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class RegexManager {

    private static List<ChatFilter> chatFilters;
    private static final Pattern pattern = Pattern.compile("(.)\\1{4,}");
    private static final List<ChatFilter> emotes = new ArrayList<>();
    public static final List<String> emotesList = new ArrayList<>();

    public static void initialize() {
        chatFilters = new ArrayList<>();

        RegexConfig.init();
        loadEmotes();
    }

    private static void loadEmotes() {
        emotes.clear();
        for(ChatFilter chatFilter : chatFilters) {
            if (chatFilter.getType() != FilterType.EMOTE) continue;

            emotes.add(chatFilter);
            emotesList.add(chatFilter.getRegex());
        }
    }

    public static void addFilter(ChatFilter filter) {
        chatFilters.add(filter);
    }

    public static boolean filterText(String playerName, UUID uuid, ModifiableString modifiableString, String channel) { // TODO loop all objects in the list and check if they violate based on the MATCHER
        return filterText(playerName, uuid, modifiableString, true, channel, null);
    }

    public static boolean filterText(String playerName, UUID uuid, ModifiableString modifiableString, boolean matcher, String channel) {
        return filterText(playerName, uuid, modifiableString, matcher, channel, null);
    }

    public static boolean filterText(String playerName, UUID uuid, ModifiableString modifiableString, boolean matcher, String channel, Consumer<FilterType> filterAction) {
        User user = ChatAPI.get().getLuckPerms().getUserManager().getUser(uuid);
        if (user == null) {
            ALogger.warn("Tried to check chat filters for a user who doesn't exist in LuckPerms");
            return false;
        }
        CachedPermissionData permissionData = user.getCachedData().getPermissionData();
        boolean isPrivate = channel.equals("party");
        for(ChatFilter chatFilter : chatFilters) {
            if (isPrivate && chatFilter.isDisabledInPrivate())
                continue;
            switch (chatFilter.getType()) {
                case CHAT:
                    break;
                case REPLACE:
                    chatFilter.replaceText(modifiableString);
                    break;
                case BLOCK:
                    if(!permissionData.checkPermission("chat.bypass-filter-channel." + channel).asBoolean() &&
                            !permissionData.checkPermission("chat.bypass-filter." + chatFilter.getName()).asBoolean() &&
                            chatFilter.matches(modifiableString)) { // todo find a better way to do this?
                        ALogger.info(playerName + " triggered the chat filter for " + chatFilter.getName()
                                + " with: " + modifiableString.string() + ".");
                        return false;
                    }
                    break;
                case REPLACEMATCHER:
                    if(matcher) {
                        chatFilter.replaceMatcher(modifiableString);
                    }
                    break;
                case PUNISH:
                    if (permissionData.checkPermission("chat.bypass-punish").asBoolean())
                        break;
                    if (chatFilter.matches(modifiableString)) {
                        ALogger.info(playerName + " triggered the punish filter for " + chatFilter.getName()
                                + " with: " + modifiableString.string() + ".");

                        if (filterAction == null){
                            ALogger.info("No filterAction was provided, not doing anything");
                            return false;
                        }
                        Player player = Bukkit.getPlayer(uuid);
                        if (player == null) {
                            ALogger.warn("Tried to punish a player who triggered the filter, but the player is offline.");
                            return false;
                        }
                        filterAction.accept(FilterType.PUNISH);
                        return false;
                    }
            }
        }
        return true;
    }

    public static List<ChatFilter> getChatFilters() {
        return chatFilters;
    }

    public static List<ChatFilter> getEmoteFilters() {
        return emotes;
    }
}
