package com.erydevs.listeners;

import com.erydevs.EryBuyer;
import com.erydevs.gui.Entry;
import com.erydevs.placeholders.BuyerPlaceholder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerInventoryListener implements Listener {

    private final EryBuyer plugin;

    public PlayerInventoryListener(EryBuyer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryChange(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        if (!plugin.getAutoBuyerManager().isAutobuyerEnabled(p)) return;
        String title = e.getView().getTitle();
        if (plugin.getBuyerGUI().isManagedTitle(title)) return;
        plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> checkAndSellItems(p), 1L);
    }

    private void checkAndSellItems(Player p) {
        if (!plugin.getAutoBuyerManager().isAutobuyerEnabled(p)) return;
        for (Entry entry : plugin.getBuyerGUI().getSlotMap().values()) {
            if (entry == null) continue;
            for (int i = 0; i < p.getInventory().getSize(); i++) {
                ItemStack is = p.getInventory().getItem(i);
                if (is == null) continue;
                if (is.getType() != entry.material) continue;
                int amount = is.getAmount();
                p.getInventory().setItem(i, null);
                double total = entry.priceX1 * amount;
                Economy econ = plugin.getEconomyManager().getEconomy();
                if (econ != null) econ.depositPlayer(p, total);
                String msg = plugin.getConfigManager().getConfig().getString("message.autosell", "&7Вы продали скупщику %item_sell% &o&6штук &7ваш баланс &o&6%balance%");
                p.sendMessage(BuyerPlaceholder.apply(msg, p, entry, amount));
                playAutobuyerSound(p);
            }
        }
    }

    private void playAutobuyerSound(Player p) {
        try {
            Sound s = Sound.valueOf(plugin.getConfigManager().getConfig().getString("autobuyer-sound"));
            p.playSound(p.getLocation(), s, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }
}
