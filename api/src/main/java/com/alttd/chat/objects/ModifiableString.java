package com.alttd.chat.objects;

public class ModifiableString {
    private String string;

    public ModifiableString(String string) {
        this.string = string;
    }

    public void string(String string) {
        this.string = string;
    }

    public void replace(String match, String replace) {
        while (string.contains(match))
            string = string.replace(match, replace);
    }

    public String string() {
        return string;
    }
}
