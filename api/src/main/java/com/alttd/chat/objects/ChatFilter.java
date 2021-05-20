package com.alttd.chat.objects;

public class ChatFilter {

    private final String regex;
    private final RegexType type;
    private String replacement = "";

    public ChatFilter(String regex, RegexType type) {
        this.regex = regex;
        this.type = type;
    }

    public ChatFilter(String regex, RegexType type, String replacement) {
        this.regex = regex;
        this.type = type;
        this.replacement = replacement;
    }

    public String getRegex() {
        return regex;
    }

    public RegexType getType() {
        return type;
    }

    public String getReplacement() {
        return replacement;
    }

}
