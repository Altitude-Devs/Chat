package com.alttd.chat.managers;

import com.alttd.chat.ChatAPI;
import com.alttd.chat.config.RegexConfig;
import com.alttd.chat.objects.ChatFilter;
import com.alttd.chat.objects.ModifiableString;
import com.alttd.chat.util.ALogger;
import net.luckperms.api.cacheddata.CachedPermissionData;
import net.luckperms.api.model.user.User;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

public class RegexManager {

    private static List<ChatFilter> chatFilters;
    private static final Pattern pattern = Pattern.compile("(.)\\1{4,}");

    public static void initialize() {
        chatFilters = new ArrayList<>();

        RegexConfig.init();
    }

    public static void addFilter(ChatFilter filter) {
        chatFilters.add(filter);
    }

    public static boolean filterText(String playerName, UUID uuid, ModifiableString modifiableString, String channel) { // TODO loop all objects in the list and check if they violate based on the MATCHER
        return filterText(playerName, uuid, modifiableString, true, channel);
    }

    public static boolean filterText(String playerName, UUID uuid, ModifiableString modifiableString, boolean matcher, String channel) {
        User user = ChatAPI.get().getLuckPerms().getUserManager().getUser(uuid);
        if (user == null) {
            ALogger.warn("Tried to check chat filters for a user who doesn't exist in LuckPerms");
            return false;
        }
        CachedPermissionData permissionData = user.getCachedData().getPermissionData();
        for(ChatFilter chatFilter : chatFilters) {
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
                        ALogger.info(playerName + " triggered the chat filter for " + chatFilter.getName() + ".");
                        return false;
                    }
                    break;
                case REPLACEMATCHER:
                    if(matcher) {
                        chatFilter.replaceMatcher(modifiableString);
                    }
                    break;
            }
        }
        return true;
    }

}
