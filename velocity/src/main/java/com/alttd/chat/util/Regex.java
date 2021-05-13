package com.alttd.chat.util;

import com.alttd.chat.config.Config;
import com.alttd.chat.config.RegexConfig;
import com.alttd.chat.config.ServerConfig;
import com.alttd.chat.objects.RegexType;
import com.google.common.collect.Lists;
import ninja.leaping.configurate.ConfigurationNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Regex {

    private static final HashMap<Pattern, ArrayList<String>> cancelRegex = new HashMap<>();
    private static final HashMap<String, String> replaceRegex = new HashMap<>();

    // IDEA: Regex object -> RegexPattern, shatteredPattern, replacement, replacements
    public static void initRegex() {
        // LOAD REGEXES, sad way of doing it:(
        // maiby a REGEXobject and a list<Regex> would be better here?
        for(ConfigurationNode node : Config.REGEXNODE.getChildrenMap().values()) {
            RegexConfig regexConfig = new RegexConfig(node.getString());
            if (RegexType.getType(regexConfig.TYPE) == RegexType.BLOCK) {
                cancelRegex.put(Pattern.compile(regexConfig.REGEX), Lists.newArrayList(regexConfig.REPLACEMENT));
            } else if (RegexType.getType(regexConfig.TYPE) == RegexType.REPLACE) {
                replaceRegex.put(regexConfig.REGEX, regexConfig.REPLACEMENT);
            }
        }

        //TODO load data from config (a regex string, and it's exceptions if there are any)
        cancelRegex.put(Pattern.compile("\\b([R]+[^\\w]?[4A]+[^\\w]?[P]+(([^\\w]?[E3]+[^\\w]?[DT]*)|([^\\w]?[I!1]+[^\\w]?[S5]+[^\\w]?[T7]+)|([^\\w]?[I!1]+[^\\w]?[N]+[^\\w]?[G69]+)))\\b"), new ArrayList<>());
        //TODO load data from config (a regex string and what to replace it with)
        replaceRegex.put(":pirate:", "pirate");

        //TODO we might just be able to throw it all in one list and just go through it and check the type to see what to do if there is a match :eyes:
    }

    public static boolean violatesFilter(String text) {
        for (Map.Entry<Pattern, ArrayList<String>> entry : cancelRegex.entrySet()) {
            Matcher matcher = entry.getKey().matcher(text);
            while (matcher.find()) {
                if (!entry.getValue().contains(matcher.group())) return true;
            }
        }
        return false;
    }

    public static String replaceText(String text) {
        for (Map.Entry<String, String> entry : replaceRegex.entrySet()) {
            text = text.replaceAll(entry.getKey(), entry.getValue());
        }
        return text;
    }

}
