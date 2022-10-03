package com.alttd.chat.nicknames;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.database.Queries;
import com.alttd.chat.events.NickEvent;
import com.alttd.chat.objects.Nick;
import com.alttd.chat.util.Utility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class NicknamesGui implements Listener {

    private final Inventory inv;
    private final int currentPage;

    public NicknamesGui() {
        // Create a new inventory, with no owner (as this isn't a real inventory)
        inv = Bukkit.createInventory(null, 36, Utility.parseMiniMessage("Nicknames GUI"));

        // Put the items into the inventory
        currentPage = 1;
        setItems(currentPage);
    }

    public void setItems(int currentPage) {
        new BukkitRunnable() {
            @Override
            public void run() {
                inv.clear();
                NickUtilities.updateCache();
                boolean hasNextPage = false;
                int i = (currentPage - 1) * 27; //TODO set to 1 or 2 to test
                int limit = i / 27;

                for (Nick nick : Nicknames.getInstance().NickCache.values()) {
                    if (nick.hasRequest()) {
                        if (limit >= i / 27) {
                            inv.setItem(i % 27, createPlayerSkull(nick, Config.NICK_ITEM_LORE));
                            i++;
                        } else {
                            hasNextPage = true;
                            break;
                        }
                    }
                }

                if (currentPage != 1) {
                    inv.setItem(28, createGuiItem(Material.PAPER, "§bPrevious page",
                            "§aCurrent page: %page%".replace("%page%", String.valueOf(currentPage)),
                            "§aPrevious page: %previousPage%".replace("%previousPage%", String.valueOf(currentPage - 1))));
                }

                if (hasNextPage) {
                    inv.setItem(36, createGuiItem(Material.PAPER, "§bNext page",
                            "§aCurrent page: %page%".replace("%page%", String.valueOf(currentPage)),
                            "§aNext page: §b%nextPage%".replace("%nextPage%", String.valueOf(currentPage + 1))));
                }
            }
        }.runTaskAsynchronously(ChatPlugin.getInstance());
    }

    private ItemStack createPlayerSkull(Nick nick, List<String> lore) {
        MiniMessage miniMessage = MiniMessage.miniMessage();
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(nick.getUuid());

        meta.setOwningPlayer(offlinePlayer);
        String name = offlinePlayer.getName();
        if (name == null)
            meta.displayName(miniMessage.deserialize("UNKNOWN PLAYER NAME"));
        else
            meta.displayName(miniMessage.deserialize(offlinePlayer.getName()));

        TagResolver resolver = TagResolver.resolver(
                Placeholder.unparsed("newnick", nick.getNewNick()),
                Placeholder.unparsed("oldnick", nick.getCurrentNick() == null ? "None" : nick.getCurrentNick()),
                Placeholder.unparsed("lastChanged", nick.getLastChangedDate() == 0 ? "Not Applicable" : nick.getLastChangedDateFormatted()));
        meta.lore(lore.stream().map(a -> miniMessage.deserialize(a, resolver)).collect(Collectors.toList()));
        playerHead.setItemMeta(meta);

        return playerHead;
    }

    // Nice little method to create a gui item with a custom name, and description
    private ItemStack createGuiItem(final Material material, final String name, final String... lore) {
        final ItemStack item = new ItemStack(material, 1);
        final ItemMeta meta = item.getItemMeta();

        // Set the name of the item
        meta.displayName(Component.text(name));

        // Set the lore of the item
        MiniMessage miniMessage = MiniMessage.miniMessage();
        meta.lore(Arrays.stream(lore).map(miniMessage::deserialize).collect(Collectors.toList()));

        item.setItemMeta(meta);

        return item;
    }

    // You can open the inventory with this
    public void openInventory(final HumanEntity ent) {//Possibly with a boolean to show if it should get from cache or update cache
        ent.openInventory(inv);
    }

    // Check for clicks on items
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getInventory() != inv) return;

        e.setCancelled(true);

        final ItemStack clickedItem = e.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        final Player p = (Player) e.getWhoClicked();

        if (clickedItem.getType().equals(Material.PAPER)) {
            String serialize = PlainTextComponentSerializer.plainText().serialize(clickedItem.getItemMeta().displayName());
            if (serialize.equals("Next Page")) {
                setItems(currentPage + 1);
            }
        } else if (clickedItem.getType().equals(Material.PLAYER_HEAD)) {
            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            if (meta.hasEnchants()) {
                return;
            }
            OfflinePlayer owningPlayer = meta.getOwningPlayer();

            if (owningPlayer == null) {
                p.sendMessage(MiniMessage.miniMessage().deserialize(Config.NICK_USER_NOT_FOUND));
                return;
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    NickUtilities.updateCache();

                    Nick nick;
                    UUID uniqueId = owningPlayer.getUniqueId();
                    if (Nicknames.getInstance().NickCache.containsKey(uniqueId)) {
                        nick = Nicknames.getInstance().NickCache.get(uniqueId);
                    } else {
                        nick = Queries.getNick(uniqueId);
                    }

                    if (nick == null || !nick.hasRequest()) {
                        p.sendMessage(MiniMessage.miniMessage().deserialize(Config.NICK_ALREADY_HANDLED,
                                Placeholder.component("targetplayer", clickedItem.getItemMeta().displayName())));
                        return;
                    }

                    if (e.isLeftClick()) {
                        if (owningPlayer.hasPlayedBefore()) {
                            Queries.acceptNewNickname(uniqueId, nick.getNewNick());

                            String newNick = nick.getNewNick();

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    NickEvent nickEvent = new NickEvent(e.getWhoClicked().getName(), clickedItem.getItemMeta().getDisplayName(), newNick, NickEvent.NickEventType.ACCEPTED);
                                    nickEvent.callEvent();
                                }
                            }.runTask(ChatPlugin.getInstance());

                            p.sendMessage(MiniMessage.miniMessage().deserialize(Config.NICK_ACCEPTED,
                                    Placeholder.component("targetplayer", clickedItem.getItemMeta().displayName()),
                                    Placeholder.unparsed("newnick", nick.getNewNick()),
                                    Placeholder.unparsed("oldnick", nick.getCurrentNick() == null ? clickedItem.getItemMeta().getDisplayName() : nick.getCurrentNick())));

                            if (owningPlayer.isOnline() && owningPlayer.getPlayer() != null) {
                                Nicknames.getInstance().setNick(owningPlayer.getUniqueId(), nick.getNewNick());
//                                owningPlayer.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(Config.NICK_CHANGED // This message is also send when the plugin message is received
//                                        .replace("%nickname%", nick.getNewNick())));
                            }

                            NickUtilities.bungeeMessageHandled(uniqueId, e.getWhoClicked().getServer().getPlayer(e.getWhoClicked().getName()), "Accepted");

                            nick.setCurrentNick(nick.getNewNick());
                            nick.setLastChangedDate(new Date().getTime());
                            nick.setNewNick(null);
                            nick.setRequestedDate(0);

                            Nicknames.getInstance().NickCache.put(uniqueId, nick);

                            ItemStack itemStack = new ItemStack(Material.SKELETON_SKULL);
                            ItemMeta itemMeta = itemStack.getItemMeta();
                            itemMeta.displayName(clickedItem.getItemMeta().displayName());
                            itemMeta.lore(clickedItem.lore());
                            itemStack.setItemMeta(itemMeta);
                            e.getInventory().setItem(e.getSlot(), itemStack);
                            p.updateInventory();
                        } else {
                            p.sendMessage(MiniMessage.miniMessage().deserialize(Config.NICK_PLAYER_NOT_ONLINE,
                                    Placeholder.component("playerName", clickedItem.getItemMeta().displayName())));
                        }

                    } else if (e.isRightClick()) {
                        Component displayName = clickedItem.getItemMeta().displayName();
                        if (owningPlayer.hasPlayedBefore()) {
                            Queries.denyNewNickname(uniqueId);

                            String newNick = nick.getNewNick();

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    NickEvent nickEvent = new NickEvent(e.getWhoClicked().getName(), clickedItem.getItemMeta().getDisplayName(), newNick, NickEvent.NickEventType.DENIED);
                                    nickEvent.callEvent();
                                }
                            }.runTask(ChatPlugin.getInstance());

                            p.sendMessage(MiniMessage.miniMessage().deserialize(Config.NICK_DENIED,
                                    Placeholder.unparsed("targetplayer", owningPlayer.getName()),
                                    Placeholder.unparsed("newnick", nick.getNewNick()),
                                    Placeholder.unparsed("oldnick", nick.getCurrentNick() == null ? owningPlayer.getName() : nick.getCurrentNick())));

                            if (Nicknames.getInstance().NickCache.containsKey(uniqueId)
                                    && Nicknames.getInstance().NickCache.get(uniqueId).getCurrentNick() != null) {
                                nick.setNewNick(null);
                                nick.setRequestedDate(0);
                                Nicknames.getInstance().NickCache.put(uniqueId, nick);
                            } else {
                                Nicknames.getInstance().NickCache.remove(uniqueId);
                            }

                            if (owningPlayer.isOnline() && owningPlayer.getPlayer() != null) {
                                Nicknames.getInstance().setNick(owningPlayer.getUniqueId(), nick.getCurrentNick() == null ? owningPlayer.getName() : nick.getCurrentNick());
                                owningPlayer.getPlayer().sendMessage(MiniMessage.miniMessage().deserialize(Config.NICK_NOT_CHANGED));
                            }

                            NickUtilities.bungeeMessageHandled(uniqueId, e.getWhoClicked().getServer().getPlayer(e.getWhoClicked().getName()), "Denied");
                            final Component messageDenied = MiniMessage.miniMessage().deserialize("<red><name>'s nickname was denied!",
                                    Placeholder.unparsed("name", owningPlayer.getName()));
                            ChatPlugin.getInstance().getServer().getOnlinePlayers().forEach(p -> {
                                if (p.hasPermission("utility.nick.review")) {
                                    p.sendMessage(messageDenied);
                                }
                            });


                            ItemStack itemStack = new ItemStack(Material.SKELETON_SKULL);
                            ItemMeta itemMeta = itemStack.getItemMeta();
                            itemMeta.displayName(displayName);
                            itemMeta.lore(clickedItem.lore());
                            itemStack.setItemMeta(itemMeta);
                            e.getInventory().setItem(e.getSlot(), itemStack);
                            p.updateInventory();
                        } else {
                            if (displayName == null)
                                p.sendMessage(MiniMessage.miniMessage().deserialize(Config.NICK_PLAYER_NOT_ONLINE, Placeholder.parsed("playerName", "UNKNOWN PLAYER NAME")));
                            else
                                p.sendMessage(MiniMessage.miniMessage().deserialize(Config.NICK_PLAYER_NOT_ONLINE, Placeholder.component("playerName", displayName)));
                        }
                    }
                }
            }.runTaskAsynchronously(ChatPlugin.getInstance());
        }
    }

    // Cancel dragging in our inventory
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryDragEvent e) {
        if (e.getInventory() == inv) {
            e.setCancelled(true);
        }
    }
}
