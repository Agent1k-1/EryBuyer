package com.erydevs.listeners;

import com.erydevs.EryBuyer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class PlayerInventoryListener implements Listener {

    private final EryBuyer plugin;

    public PlayerInventoryListener(EryBuyer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryChange(InventoryClickEvent e) {
    }
}
