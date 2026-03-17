package com.erydevs.listeners;

import com.erydevs.EryBuyer;
import com.erydevs.gui.Entry;
import com.erydevs.gui.BuyerSite;
import com.erydevs.placeholders.PlaceholderAPIHook;
import com.erydevs.gui.action.ActionMenu;
import com.erydevs.sound.Sounds;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final EryBuyer plugin;
    private final Sounds sounds;

    public InventoryListener(EryBuyer plugin) {
        this.plugin = plugin;
        this.sounds = new Sounds(plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        if (e.getView() == null) return;
        String title = e.getView().getTitle();
        if (!plugin.getBuyerGUI().isManagedTitle(title)) return;
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        java.util.List<String> acts = plugin.getBuyerGUI().getActions(title, slot);
        if (acts != null && !acts.isEmpty()) {
            ActionMenu.execute(plugin, p, acts);
            return;
        }

        com.erydevs.config.Configuration confMgr = plugin.getConfigManager();
        String menuName = plugin.getBuyerGUI().getMenuNameByTitle(title);
        org.bukkit.configuration.file.FileConfiguration menuCfg = confMgr.getMenuConfig(menuName);
        
        int exitSlot = plugin.getBuyerGUI().getExitSlot(title, menuCfg);
        if (exitSlot >= 0 && slot == exitSlot) {
            p.closeInventory();
            return;
        }

        int autobuyerSlot = plugin.getBuyerGUI().getAutobuyerSlot(title, menuCfg);
        if (autobuyerSlot >= 0 && slot == autobuyerSlot) {
            plugin.getAutoBuyerManager().toggleAutobuyer(p);
            boolean enabled = plugin.getAutoBuyerManager().isAutobuyerEnabled(p);
            String path = enabled ? "message.auto-buyer-on" : "message.auto-buyer-off";
            String raw = plugin.getConfigManager().getConfig().getString(path, enabled ? "&7Автоскупщик &aвключён" : "&7Автоскупщик &cвыключен");
            p.sendMessage(PlaceholderAPIHook.apply(raw, p));
            p.openInventory(plugin.getBuyerGUI().createInventory(p, menuName));
            sounds.playMenuOpenSound(p);
            return;
        }
        
        if (plugin.getAutoBuyerManager().isAutobuyerEnabled(p)) {
            return;
        }

        Entry entry = plugin.getBuyerGUI().getEntry(title, slot);
        if (entry == null) return;
        if (entry.priceX1 <= 0) return;
        BuyerSite.ClickType clickType = e.isLeftClick() ? BuyerSite.ClickType.LEFT : BuyerSite.ClickType.RIGHT;
        if (clickType == BuyerSite.ClickType.LEFT) {
            processSale(p, entry, 1, entry.priceX1);
            return;
        }
        if (clickType == BuyerSite.ClickType.RIGHT) {
            int totalCount = countItemsInInventory(p, entry.material);
            if (totalCount == 0) {
                String msg = plugin.getConfigManager().getConfig().getString("message.no-item");
                p.sendMessage(PlaceholderAPIHook.apply(msg, p, entry, 64));
                sounds.playNoItemSound(p);
                return;
            }
            if (totalCount < 64) return;
            processSale(p, entry, 64, entry.priceX1);
        }
    }

    private int countItemsInInventory(Player p, org.bukkit.Material material) {
        int total = 0;
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack is = p.getInventory().getItem(i);
            if (is != null && is.getType() == material) {
                total += is.getAmount();
            }
        }
        return total;
    }

    private int removeItemsFromInventory(Player p, Entry entry, int amountNeeded) {
        int removed = 0;
        for (int i = 0; i < p.getInventory().getSize() && removed < amountNeeded; i++) {
            ItemStack is = p.getInventory().getItem(i);
            if (is == null || is.getType() != entry.material) continue;
            int canRemove = Math.min(is.getAmount(), amountNeeded - removed);
            if (is.getAmount() > canRemove) {
                is.setAmount(is.getAmount() - canRemove);
                p.getInventory().setItem(i, is);
            } else {
                p.getInventory().setItem(i, null);
            }
            removed += canRemove;
        }
        return removed;
    }

    private void processSale(Player p, Entry entry, int requestedAmount, double unitPrice) {
        int actualAmount = removeItemsFromInventory(p, entry, requestedAmount);
        
        if (actualAmount == 0) {
            String msg = plugin.getConfigManager().getConfig().getString("message.no-item");
            p.sendMessage(PlaceholderAPIHook.apply(msg, p, entry, requestedAmount));
            sounds.playNoItemSound(p);
            return;
        }
        
        com.erydevs.levels.PlayerLevel playerLevel = plugin.getDataBase().getPlayerData(p.getUniqueId());
        double basePrice = unitPrice * actualAmount;
        double multiplier = 1.0 + plugin.getLevelConfig().getMultiplierByLevel(playerLevel.getCurrentLevel());
        double totalPrice = basePrice * multiplier;
        Economy econ = plugin.getEconomyManager().getEconomy();
        if (econ != null) econ.depositPlayer(p, totalPrice);
        
        int maxLevel = plugin.getLevelConfig().getMaxLevel();
        if (playerLevel.getCurrentLevel() < maxLevel) {
            plugin.getDataBase().addPlayerEarnings(p.getUniqueId(), basePrice);
            checkAndUpdateLevel(p);
        }
        
        String raw = plugin.getConfigManager().getConfig().getString("message.successfully-buyer");
        p.sendMessage(PlaceholderAPIHook.apply(raw, p, entry, actualAmount, totalPrice));
    }

    private void checkAndUpdateLevel(Player p) {
        com.erydevs.levels.PlayerLevel playerLevel = plugin.getDataBase().getPlayerData(p.getUniqueId());
        int maxLevel = plugin.getLevelConfig().getMaxLevel();
        double totalEarned = playerLevel.getTotalEarned();
        
        while (playerLevel.getCurrentLevel() < maxLevel) {
            int nextLevel = playerLevel.getCurrentLevel() + 1;
            if (plugin.getLevelConfig().getRequiredMoneyForLevel(nextLevel) <= totalEarned) {
                playerLevel.setCurrentLevel(nextLevel);
                plugin.getDataBase().savePlayerData(playerLevel);
                String msg = plugin.getConfigManager().getConfig().getString("message.level-up");
                p.sendMessage(com.erydevs.placeholders.PlaceholderAPIHook.applyLevelUp(msg, p, nextLevel));
            } else {
                break;
            }
        }
    }

}