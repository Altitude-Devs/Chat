package com.alttd.chat.objects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ModifiableString {
    private Component text;

    public ModifiableString(Component text) {
        this.text = text;
    }

    public void string(Component text) {
        this.text = text;
    }

    public void replace(TextReplacementConfig textReplacementConfig) {
        text = text.replaceText(textReplacementConfig);
    }

    public String string() {
        return PlainTextComponentSerializer.plainText().serialize(text);
    }

    public Component component() {
        return text;
    }

    public void reverse() {
        text = reverseComponent(text);
    }

    public Component reverseComponent(Component component) {
        if (!(component instanceof TextComponent textComponent)) {
            return Component.text("")
                    .append(Component.join(JoinConfiguration.noSeparators(), reverseChildren(component.children())));
        }

        String content = textComponent.content();
        String reversedContent = new StringBuilder(content).reverse().toString();

        List<Component> reversedChildren = reverseChildren(component.children());

        return Component.text("")
                .append(Component.join(JoinConfiguration.noSeparators(), reversedChildren)
                        .append(Component.text(reversedContent, component.style())));
    }

    public List<Component> reverseChildren(List<Component> children) {
        return children.stream()
                .map(this::reverseComponent)
                .collect(Collectors.collectingAndThen(Collectors.toList(), list -> {
                    Collections.reverse(list);
                    return list;
                }));
    }

    public void removeStringAtStart(String s) {
        text = text.replaceText(TextReplacementConfig.builder().match("^" + s).replacement("").build());
    }
}
