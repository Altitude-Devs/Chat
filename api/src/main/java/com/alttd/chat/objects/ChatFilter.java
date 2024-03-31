package com.alttd.chat.objects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFilter {

    private final String name;
    private final FilterType filterType;
    private final String regex;
    private final Pattern pattern;
    private final String replacement;
    private final List<String> exclusions;
    private final boolean disableInPrivate;

    public ChatFilter(String name, String type, String regex, String replacement, List<String> exclusions, boolean disableInPrivate) {
        this.name = name;
        this.filterType = FilterType.getType(type);
        this.regex = regex;
        this.pattern = Pattern.compile(getRegex(), Pattern.CASE_INSENSITIVE);
        this.replacement = replacement;
        this.exclusions = exclusions;
        this.disableInPrivate = disableInPrivate;
    }

    public String getName() {
        return this.name;
    }

    public String getRegex() {
        return this.regex;
    }

    public FilterType getType() {
        return this.filterType;
    }

    public String getReplacement() {
        return this.replacement;
    }

    public List<String> getExclusions() {
        return this.exclusions;
    }

    public boolean isDisabledInPrivate() {
        return disableInPrivate;
    }

    public boolean matches(ModifiableString filterableString) {
        String input = filterableString.string();
        Matcher matcher = pattern.matcher(input);
        while (matcher.find())
            if (!isException(input, matcher.start())) {
//                filterableString.string(filterableString.string().replaceFirst(matcher.group(), "<gold>" + matcher.group() + "</gold>"));
                filterableString.string(filterableString.component()
                        .replaceText(
                        TextReplacementConfig.builder()
                                .match(matcher.pattern())
                                .replacement(Component.text(matcher.group()).style(Style.style(NamedTextColor.GOLD)))
                                .build()));
                return true;
            }
        return matcher.matches();
    }

    public boolean isException(String string, int start)
    {
        char[] chars = string.toCharArray();
        if (start != 0) { //go to start of word if not at start of string
            while (chars[start] != ' ' && start > 0)
                start--;
            start += 1; //go past the space
        }

        String match = string.substring(start);
        for (String s : getExclusions()) {
            if (match.toLowerCase().startsWith(s.toLowerCase()))
                return true;
        }
        return false;
    }

    public void replaceText(ModifiableString modifiableString) {
        String input = modifiableString.string();
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group(); // todo debug
            if (getExclusions().stream().noneMatch(s -> s.equalsIgnoreCase(group))) { // idk how heavy this is:/
                modifiableString.replace(
                        TextReplacementConfig.builder()
                                .matchLiteral(group)
                                .replacement(getReplacement())
                                .build()
                );
            }
        }
    }

    public void replaceMatcher(ModifiableString modifiableString) {
        String input = modifiableString.string();
        int length;
        try {
            length = Integer.parseInt(replacement);
        } catch (NumberFormatException e) {
            length = 3; // could load this from config and make it cleaner
        }
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group();
            modifiableString.replace(TextReplacementConfig.builder()
                    .matchLiteral(group)
                    .replacement(Component.text(group.substring(0, length), modifiableString.component().color()))
                    .build());
        }
    }
}
