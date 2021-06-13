package com.alttd.chat.objects;

public enum FilterType {
    REPLACE("replace"),
    CHAT("chat"),
    REPLACEMATCHER("replacematcher"),
    BLOCK("block");

    private final String name;

    FilterType(String name) {
        this.name = name;
    }

    public static FilterType getType(String name) {
        for (FilterType type : FilterType.values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }
}
