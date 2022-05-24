package com.alttd.chat.objects;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Toggleable {

    private static final List<Toggleable> togglableClasses = new ArrayList<>();

    public Toggleable() {
        togglableClasses.add(this);
    }

    public static Toggleable getToggleable(UUID uuid) {
        for (Toggleable toggleableClass : togglableClasses) {
            if (toggleableClass.isToggled(uuid))
                return toggleableClass;
        }
        return null;
    }

    public abstract boolean isToggled(UUID uuid);

    public abstract void setOff(UUID uuid);

    public boolean toggle(UUID uuid) {
        if (isToggled(uuid)) {
            setOff(uuid);
            return (false);
        }
        for (Toggleable toggleable : togglableClasses) {
            if (toggleable.isToggled(uuid)) {
                setOff(uuid);
                break;
            }
        }
        setOn(uuid);
        return (true);
    }

    public abstract void setOn(UUID uuid);

    public static void disableToggles(UUID uuid) {
        for (Toggleable toggleable : togglableClasses) {
            if (toggleable.isToggled(uuid)) {
                toggleable.setOff(uuid);
            }
        }
    }

    public void sendMessage(Player player, Component message) {
        sendMessage(player, PlainTextComponentSerializer.plainText().serialize(message));
    }

    public abstract void sendMessage(Player player, String message);

}
