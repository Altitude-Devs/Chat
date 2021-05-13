package com.alttd.velocitychat.objects;

public enum RegexType {
    REPLACE("replace"),
    BLOCK("block");

    private final String name;

    RegexType(String name) {
        this.name = name;
    }

    public static RegexType getType(String name) {
        for (RegexType type : RegexType.values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }
}
