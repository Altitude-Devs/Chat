package com.alttd.velocitychat.objects;

public class Regex {

    private final String regex;
    private final RegexType type;
    private String replacement = "";

    public Regex(String regex, RegexType type) {
        this.regex = regex;
        this.type = type;
    }

    public Regex(String regex, RegexType type, String replacement) {
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
