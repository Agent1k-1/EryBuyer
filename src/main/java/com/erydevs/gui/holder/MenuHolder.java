package com.erydevs.gui.holder;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public final class MenuHolder implements InventoryHolder {
    private final String menuId;
    private Inventory inventory;

    public MenuHolder(@NotNull String menuId) {
        this.menuId = menuId;
    }

    @NotNull
    public String getMenuId() {
        return menuId;
    }

    public void bind(@NotNull Inventory inventory) {
        if (this.inventory != null) {
            throw new IllegalStateException("Инвентарь меню уже установлен");
        }
        this.inventory = inventory;
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        if (inventory == null) {
            throw new IllegalStateException("Инвентарь меню ещё не установлен");
        }
        return inventory;
    }
}
