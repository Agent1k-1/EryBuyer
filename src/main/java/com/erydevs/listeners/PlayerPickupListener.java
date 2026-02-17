package com.erydevs.listeners;

import com.erydevs.EryBuyer;
import com.erydevs.gui.Entry;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerPickupListener implements Listener {

    private final EryBuyer plugin;

    public PlayerPickupListener(EryBuyer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player)) return;
        Player p = (Player) e.getEntity();
        if (!plugin.getAutoBuyerManager().isAutobuyerEnabled(p)) return;

        ItemStack item = e.getItem().getItemStack();
        if (item == null) return;

        for (Entry entry : plugin.getBuyerGUI().getAllEntries()) {
            if (entry == null || entry.material == null) continue;
            if (entry.material != item.getType()) continue;

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                plugin.getAutoBuyerManager().processPlayerInventory(p);
            }, 2L);
            return;
        }
    }
}
