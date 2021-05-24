package com.alttd.chat.data;

public class ChatFilter {

    private final String regex;
    private final FilterType type;
    private String replacement = "";

    public ChatFilter(String regex, FilterType type) {
        this.regex = regex;
        this.type = type;
    }

    public ChatFilter(String regex, FilterType type, String replacement) {
        this.regex = regex;
        this.type = type;
        this.replacement = replacement;
    }

    public String getRegex() {
        return regex;
    }

    public FilterType getType() {
        return type;
    }

    public String getReplacement() {
        return replacement;
    }

}
