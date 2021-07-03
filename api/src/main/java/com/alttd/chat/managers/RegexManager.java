package com.alttd.chat.managers;

import com.alttd.chat.config.RegexConfig;
import com.alttd.chat.objects.ChatFilter;
import com.alttd.chat.util.ALogger;

import java.util.ArrayList;
import java.util.List;

public class RegexManager {

    private static List<ChatFilter> chatFilters;

    public static void initialize() {
        chatFilters = new ArrayList<>();

        RegexConfig.init();
    }

    public static void addFilter(ChatFilter filter) {
        chatFilters.add(filter);
    }

    public static String replaceText(String text) { // TODO loop all objects in the list and check if they violate based on the MATCHER
        for(ChatFilter chatFilter : chatFilters) {
            switch (chatFilter.getType()) {
                case CHAT:
                    break;
                case REPLACE:
                    text = chatFilter.replaceText(text);
                    break;
                case BLOCK:
                    if(chatFilter.matches(text)) { // todo find a better way to do this?
                        return null;
                    }
                    break;
            }
        }
        return text;
    }

}
