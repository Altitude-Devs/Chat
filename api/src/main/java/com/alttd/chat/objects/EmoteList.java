package com.alttd.chat.objects;

import com.alttd.chat.config.Config;
import com.alttd.chat.managers.RegexManager;
import com.alttd.chat.util.Utility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EmoteList {

    public static final Map<UUID, EmoteList> emoteLists = new HashMap<>();
    public static final int pageSize = 7;
//    public static int pages = RegexManager.getEmoteFilters().size() / pageSize;// TODO reload this when config is reloaded.

    public static EmoteList getEmoteList(UUID uuid) {
        synchronized (emoteLists) {
            return emoteLists.computeIfAbsent(uuid, k -> new EmoteList());
        }
    }

    private int page;
    public Component showEmotePage() {
        int startIndex = page * pageSize;
        int pages = RegexManager.getEmoteFilters().size() / pageSize;
        int endIndex = Math.min(startIndex + pageSize, RegexManager.getEmoteFilters().size());
        TagResolver placeholders = TagResolver.resolver(
                Placeholder.unparsed("page", String.valueOf(page)),
                Placeholder.unparsed("pages", String.valueOf(pages))
        );
        Component list = Utility.parseMiniMessage(Config.EMOTELIST_HEADER, placeholders);

        for (int i = startIndex; i < endIndex; i++) {
            ChatFilter emote = RegexManager.getEmoteFilters().get(i);
            TagResolver emotes = TagResolver.resolver(
                    Placeholder.parsed("regex", emote.getRegex()),
                    Placeholder.parsed("emote", emote.getReplacement())
            );
            list = list.append(Utility.parseMiniMessage(Config.EMOTELIST_ITEM, emotes));
        }
        list = list.append(Utility.parseMiniMessage(Config.EMOTELIST_FOOTER, placeholders));
        return list;
    }

    public void setPage(int page) {
        this.page = Math.min(page, RegexManager.getEmoteFilters().size() / pageSize);
    }

    public void nextPage() {
        this.page = Math.min(page + 1, RegexManager.getEmoteFilters().size() / pageSize);
    }

    public void prevPage() {
        this.page -= 1;
        this.page = Math.max(page - 1, 0);
    }


}
