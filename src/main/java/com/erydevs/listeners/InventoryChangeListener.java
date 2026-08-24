package com.erydevs.listeners;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

public class InventoryChangeListener implements Listener {

    private final EryBuyer plugin;

    public InventoryChangeListener(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickup(@NotNull EntityPickupItemEvent e) {
        if (e.getEntity() instanceof Player) {
            markDirty((Player) e.getEntity());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClick(@NotNull InventoryClickEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            markDirty((Player) e.getWhoClicked());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onCraft(@NotNull CraftItemEvent e) {
        if (e.getWhoClicked() instanceof Player) {
            markDirty((Player) e.getWhoClicked());
        }
    }

    private void markDirty(@NotNull Player player) {
        if (!plugin.getAutoBuyerManager().isAutobuyerEnabled(player)) return;
        plugin.getAutoBuyerManager().markDirty(player);
    }
}
