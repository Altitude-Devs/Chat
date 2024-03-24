import com.alttd.chat.config.Config;
import com.alttd.chat.objects.ModifiableString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ReverseTest {

    @Test
    public void testReverseString() {
        String input = "Hello how are you doing today?";
        String expectedOutput = new StringBuilder(input).reverse().toString();
        MiniMessage miniMessage = MiniMessage.miniMessage();
        ModifiableString modifiableString = new ModifiableString(miniMessage.deserialize(input));
        modifiableString.reverse();
        assertEquals(expectedOutput, modifiableString.string());
    }

    @Test
    public void testRemoveKeyword() {
        String input = "Hello how are you doing today?";
        MiniMessage miniMessage = MiniMessage.miniMessage();

        ModifiableString modifiableString = new ModifiableString(miniMessage.deserialize(Config.APRIL_FOOLS_RESET + " " + input));
        modifiableString.removeStringAtStart(Config.APRIL_FOOLS_RESET + " ");

        assertEquals(input, modifiableString.string());
    }

    @Test
    public void testRemoveKeywordWithTags() {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        String input = "<blue>" + Config.APRIL_FOOLS_RESET + " <red>Hello how are</red> you <blue>doing today</blue>?</blue>";
        String expectedOutput = "Hello how are you doing today?";
        Component deserialize = miniMessage.deserialize(input);

        ModifiableString modifiableString = new ModifiableString(deserialize);
        modifiableString.removeStringAtStart(Config.APRIL_FOOLS_RESET + " ");

        assertEquals(expectedOutput, modifiableString.string());
    }


    @Test
    public void testReverseStringWithTags() {
        String input = "<red>Hello how are</red> you <blue>doing today</blue>?";
        MiniMessage miniMessage = MiniMessage.miniMessage();
        Component deserialize = miniMessage.deserialize(input);
        ModifiableString modifiableString = new ModifiableString(deserialize);
        String expectedOutput = new StringBuilder(PlainTextComponentSerializer.plainText().serialize(deserialize)).reverse().toString();
        modifiableString.reverse();
        assertEquals(expectedOutput, modifiableString.string());
    }

    @Test
    public void complexTestReverseStringWithTags() {
        String input = "<green><red>Hello <b>how</b> are</red> you <blue>doing today</blue><gold>?</gold></green>";
        MiniMessage miniMessage = MiniMessage.miniMessage();
        Component deserialize = miniMessage.deserialize(input);
        ModifiableString modifiableString = new ModifiableString(deserialize);
        String expectedOutput = new StringBuilder(PlainTextComponentSerializer.plainText().serialize(deserialize)).reverse().toString();
        modifiableString.reverse();
        assertEquals(expectedOutput, modifiableString.string());
    }

    @Test
    public void extraComplexTestReverseStringWithTags() {
        String input = "<gold>This <red>is</red> longer<green> <name> <red>Hello <b>how</b> are</red> you <test> <blue>doing <name> today</blue><gold>?</gold></green></gold>";
        MiniMessage miniMessage = MiniMessage.miniMessage();
        Component deserialize = miniMessage.deserialize(input, TagResolver.resolver(
                Placeholder.component("name", miniMessage.deserialize("<red>Cool<blue><rainbow>_player_</rainbow>name</red>")),
                Placeholder.parsed("test", "test replacement")
        ));
        ModifiableString modifiableString = new ModifiableString(deserialize);
        String expectedOutput = new StringBuilder(PlainTextComponentSerializer.plainText().serialize(deserialize)).reverse().toString();
        modifiableString.reverse();
        System.out.println(expectedOutput);
        assertEquals(expectedOutput, modifiableString.string());
    }

}
