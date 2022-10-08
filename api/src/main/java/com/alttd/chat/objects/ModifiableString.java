package com.alttd.chat.objects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class ModifiableString {
    private Component text;

    public ModifiableString(Component text) {
        this.text = text;
    }

    public void string(Component text) {
        this.text = text;
    }

    public void replace(String match, String replace) {
        text = text
                .replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral(match)
                                .replacement(replace)
                                .build());
    }

    public String string() {
        return PlainTextComponentSerializer.plainText().serialize(text);
    }

    public Component component() {
        return text;
    }
}
