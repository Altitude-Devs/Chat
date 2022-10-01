package com.alttd.chat.nicknames;

import com.alttd.chat.ChatPlugin;
import com.alttd.chat.config.Config;
import com.alttd.chat.database.Queries;
import com.alttd.chat.events.NickEvent;
import com.alttd.chat.objects.Nick;
import com.alttd.chat.util.Utility;
import net.kyori.adventure.text.Component;
import net.md_5.bungee.api.ChatColor;
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

import static com.alttd.chat.nicknames.Nicknames.format;

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
        ItemStack playerHead = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) playerHead.getItemMeta();
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(nick.getUuid());

        meta.setOwningPlayer(offlinePlayer);
        meta.setDisplayName(offlinePlayer.getName());

        lore.replaceAll(s -> format(s
                .replace("%newNick%", nick.getNewNick())
                .replace("%oldNick%", nick.getCurrentNick() == null ? "None" : nick.getCurrentNick())
                .replace("%lastChanged%", nick.getLastChangedDate() == 0 ? "Not Applicable" : nick.getLastChangedDateFormatted())));

        meta.setLore(lore);
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
        meta.setLore(Arrays.asList(lore));

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
            if (clickedItem.getItemMeta().getDisplayName().equals("Next Page")) {
                setItems(currentPage + 1);
            }
        } else if (clickedItem.getType().equals(Material.PLAYER_HEAD)) {
            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            if (meta.hasEnchants()) {
                return;
            }
            OfflinePlayer owningPlayer = meta.getOwningPlayer();

            if (owningPlayer == null) {
                p.sendMessage(format(Config.NICK_USER_NOT_FOUND));
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
                        p.sendMessage(format(Config.NICK_ALREADY_HANDLED
                                .replace("%targetPlayer%", clickedItem.getItemMeta().getDisplayName())));
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

                            p.sendMessage(format(Config.NICK_ACCEPTED
                                    .replace("%targetPlayer%", clickedItem.getItemMeta().getDisplayName())
                                    .replace("%newNick%", nick.getNewNick())
                                    .replace("%oldNick%", nick.getCurrentNick() == null ? clickedItem.getItemMeta().getDisplayName() : nick.getCurrentNick())));

                            if (owningPlayer.isOnline() && owningPlayer.getPlayer() != null) {
                                Nicknames.getInstance().setNick(owningPlayer.getUniqueId(), nick.getNewNick());
//                                owningPlayer.getPlayer().sendMessage(format(Config.NICK_CHANGED // This message is also send when the plugin message is received
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
                            p.sendMessage(format(Config.NICK_PLAYER_NOT_ONLINE
                                    .replace("%playerName%", clickedItem.getItemMeta().getDisplayName())));
                        }

                    } else if (e.isRightClick()) {
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

                            p.sendMessage(format(Config.NICK_DENIED
                                    .replace("%targetPlayer%", owningPlayer.getName())
                                    .replace("%newNick%", nick.getNewNick())
                                    .replace("%oldNick%", nick.getCurrentNick() == null ? owningPlayer.getName() : nick.getCurrentNick())));

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
                                owningPlayer.getPlayer().sendMessage(format(Config.NICK_NOT_CHANGED));
                            }

                            NickUtilities.bungeeMessageHandled(uniqueId, e.getWhoClicked().getServer().getPlayer(e.getWhoClicked().getName()), "Denied");
                            final String messageDenied = ChatColor.RED + owningPlayer.getName() + "'s nickname was denied!";
                            ChatPlugin.getInstance().getServer().getOnlinePlayers().forEach(p -> {
                                if (p.hasPermission("utility.nick.review")) {
                                    p.sendMessage(messageDenied);
                                }
                            });


                            ItemStack itemStack = new ItemStack(Material.SKELETON_SKULL);
                            ItemMeta itemMeta = itemStack.getItemMeta();
                            itemMeta.displayName(clickedItem.getItemMeta().displayName());
                            itemMeta.lore(clickedItem.lore());
                            itemStack.setItemMeta(itemMeta);
                            e.getInventory().setItem(e.getSlot(), itemStack);
                            p.updateInventory();
                        } else {
                            p.sendMessage(format(Config.NICK_PLAYER_NOT_ONLINE
                                    .replace("%playerName%", clickedItem.getItemMeta().getDisplayName())));
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
