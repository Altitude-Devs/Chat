package com.alttd.chat.requests;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class RequestsGui implements InventoryHolder {

    private final Inventory inventory;
    private final int inventorySize = 54;

    RequestsGui() {
        inventory = Bukkit.createInventory(this, inventorySize, "A title");
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

}
