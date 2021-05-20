package com.alttd.chat.objects;

public enum ChatFilterType {
    REPLACE("replace"),
    BLOCK("block");

    private final String name;

    ChatFilterType(String name) {
        this.name = name;
    }

    public static ChatFilterType getType(String name) {
        for (ChatFilterType type : ChatFilterType.values()) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }

        return null;
    }
}
