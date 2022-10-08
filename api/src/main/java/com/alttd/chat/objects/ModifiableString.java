package com.alttd.chat.objects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.intellij.lang.annotations.RegExp;

import javax.annotation.RegEx;
import java.util.regex.Pattern;

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

    public void replace(Pattern match, String replace) {
        text = text
                .replaceText(
                        TextReplacementConfig.builder()
                                .match(match)
                                .replacement(replace)
                                .build());
    }

    public void replace(@RegExp String match, Component replace) {
        text = text
                .replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral(match)
                                .replacement(replace)
                                .build());
    }

    public void replace(Pattern match, Component replace) {
        text = text
                .replaceText(
                        TextReplacementConfig.builder()
                                .match(match)
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
