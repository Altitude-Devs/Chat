package com.alttd.chat.objects;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFilter {

    private final String name;
    private final FilterType filterType;
    private final String regex;
    private final Pattern pattern;
    private final String replacement;
    private final List<String> exclusions;

    public ChatFilter(String name, String type, String regex, String replacement, List<String> exclusions) {
        this.name = name;
        this.filterType = FilterType.getType(type);
        this.regex = regex;
        this.pattern = Pattern.compile(getRegex(), Pattern.CASE_INSENSITIVE);
        this.replacement = replacement;
        this.exclusions = exclusions;
    }

    public String getName() {
        return this.name;
    }

    public String getRegex() {
        return this.regex;
    }

    public FilterType getType() {
        return this.filterType;
    }

    public String getReplacement() {
        return this.replacement;
    }

    public List<String> getExclusions() {
        return this.exclusions;
    }

    public boolean matches(String input) {
        Matcher matcher = pattern.matcher(input);
        return (matcher.find() || matcher.matches());
    }

    public String replaceText(String input) {
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(); // todo debug
            if(getExclusions().stream().noneMatch(s -> s.equalsIgnoreCase(group))) { // idk how heavy this is:/
                input = input.replace(group, getReplacement());
            }
        }
        return input;
    }

    public String replaceMatcher(String input) {
        int lenght;
        try {
            lenght = Integer.parseInt(replacement);
        } catch (NumberFormatException e) {
            lenght = 3; // could load this from config and make it cleaner
        }
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group();
            input = input.replace(group, group.substring(0, lenght));
        }
        return input;
    }
}
