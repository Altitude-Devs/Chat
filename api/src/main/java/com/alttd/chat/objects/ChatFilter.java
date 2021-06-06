package com.alttd.chat.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatFilter {

    private final Properties properties = new Properties();
    private final Pattern pattern;
    private final FilterType filterType;

    public ChatFilter(Map<String, Object> props) {
        addDefaults();
        properties.keySet().stream().filter(props::containsKey).forEach((nkey) -> properties.put(nkey, props.get(nkey)));
        pattern = Pattern.compile(getRegex());
        filterType = FilterType.getType((String) properties.get("type"));
    }

    private void addDefaults() {
        properties.put("name", "");
        properties.put("type", null);
        properties.put("regex", "");
        properties.put("replacement", "");
        properties.put("exclusions", new ArrayList<String>());
    }

    public String getRegex() {
        return (String) properties.get("regex");
    }

    public FilterType getType() {
        return filterType;
    }

    public String getReplacement() {
        return (String) properties.get("replacement");
    }

    @SuppressWarnings("unchecked")
    public List<String> getExclusions() {
        return (List<String>) properties.get("exclusions");
    }

    public boolean matches(String input) {
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public String replaceText(String input) {
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String group = matcher.group();
            if(!getExclusions().contains(group)) { // doesn't work well with capitals, use a stream filter?
                input = input.replace(group, getReplacement());
            }
        }
        return input;
    }
}
