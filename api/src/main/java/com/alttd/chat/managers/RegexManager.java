package com.alttd.chat.managers;

import com.alttd.chat.config.RegexConfig;
import com.alttd.chat.objects.ChatFilter;
import com.alttd.chat.objects.FilterType;

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
//    public static boolean violatesFilter(String text) {
//        for (Map.Entry<Pattern, ArrayList<String>> entry : cancelRegex.entrySet()) {
//            Matcher matcher = entry.getKey().matcher(text);
//            while (matcher.find()) {
//                if (!entry.getValue().contains(matcher.group())) return true;
//            }
//        }
//        return false;
//    }

    public static String replaceText(String text) { // TODO loop all objects in the list and check if they violate based on the MATCHER
        for(ChatFilter chatFilter : chatFilters) {

            switch (chatFilter.getType()) {
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
//        for (Map.Entry<String, String> entry : replaceRegex.entrySet()) {
//            text = text.replaceAll(entry.getKey(), entry.getValue());
//        }
        return text;
    }

}
