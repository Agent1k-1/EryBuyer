package com.erydevs.listeners;

import com.erydevs.EryBuyer;
import com.erydevs.gui.Entry;
import com.erydevs.gui.BuyerSite;
import com.erydevs.placeholders.BuyerPlaceholder;
import com.erydevs.gui.action.ActionMenu;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {

    private final EryBuyer plugin;

    public InventoryListener(EryBuyer plugin) {
        this.plugin = plugin;
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

        if (slot == plugin.getBuyerGUI().getExitSlot(title)) {
            p.closeInventory();
            return;
        }

        if (slot == plugin.getBuyerGUI().getAutobuyerSlot(title)) {
            plugin.getAutoBuyerManager().toggleAutobuyer(p);
            boolean enabled = plugin.getAutoBuyerManager().isAutobuyerEnabled(p);
            String path = enabled ? "message.auto-buyer-on" : "message.auto-buyer-off";
            String raw = plugin.getConfigManager().getConfig().getString(path, enabled ? "&7Автоскупщик &aвключён" : "&7Автоскупщик &cвыключен");
            p.sendMessage(BuyerPlaceholder.apply(raw, p));
            String menuName = plugin.getBuyerGUI().getMenuNameByTitle(title);
            p.openInventory(plugin.getBuyerGUI().createInventory(p, menuName));
            playMenuOpenSound(p);
            return;
        }

        Entry entry = plugin.getBuyerGUI().getEntry(title, slot);
        if (entry == null) return;
        if (entry.priceX1 <= 0) return;
        BuyerSite.ClickType clickType = e.isLeftClick() ? BuyerSite.ClickType.LEFT : BuyerSite.ClickType.RIGHT;
        if (clickType == BuyerSite.ClickType.LEFT) {
            sellAmount(p, entry, 1, entry.priceX1);
            return;
        }
        if (clickType == BuyerSite.ClickType.RIGHT) {
            int totalCount = 0;
            for (int i = 0; i < p.getInventory().getSize(); i++) {
                ItemStack it = p.getInventory().getItem(i);
                if (it == null) continue;
                if (it.getType() == entry.material) totalCount += it.getAmount();
            }
            if (totalCount == 0) {
                String msg = plugin.getConfigManager().getConfig().getString("message.no-item");
                p.sendMessage(BuyerPlaceholder.apply(msg, p, entry, 64));
                playNoItemSound(p);
                return;
            }
            if (totalCount < 64) return;
            removeExactAmountAndPay(p, entry, 64, entry.priceX64);
        }
    }

    private void sellAmount(Player p, Entry entry, int want, double unitPrice) {
        int needed = want;
        int sold = 0;
        for (int i = 0; i < p.getInventory().getSize() && needed > 0; i++) {
            ItemStack is = p.getInventory().getItem(i);
            if (is == null) continue;
            if (is.getType() != entry.material) continue;
            int can = Math.min(is.getAmount(), needed);
            if (is.getAmount() > can) {
                is.setAmount(is.getAmount() - can);
                p.getInventory().setItem(i, is);
            } else {
                p.getInventory().setItem(i, null);
            }
            sold += can;
            needed -= can;
        }
        if (sold == 0) {
            String msg = plugin.getConfigManager().getConfig().getString("message.no-item");
            p.sendMessage(BuyerPlaceholder.apply(msg, p, entry, want));
            playNoItemSound(p);
            return;
        }
        double total = unitPrice * sold;
        Economy econ = plugin.getEconomyManager().getEconomy();
        if (econ != null) econ.depositPlayer(p, total);
        plugin.getDataBase().addPlayerEarnings(p.getUniqueId(), total);
        checkAndUpdateLevel(p);
        String raw = plugin.getConfigManager().getConfig().getString("message.successfully-buyer");
        raw = raw.replace("%prince%", String.format("%.2f", total));
        p.sendMessage(BuyerPlaceholder.apply(raw, p, entry, sold));
    }

    private void removeExactAmountAndPay(Player p, Entry entry, int amount, double totalPrice) {
        int needed = amount;
        for (int i = 0; i < p.getInventory().getSize() && needed > 0; i++) {
            ItemStack is = p.getInventory().getItem(i);
            if (is == null) continue;
            if (is.getType() != entry.material) continue;
            int can = Math.min(is.getAmount(), needed);
            if (is.getAmount() > can) {
                is.setAmount(is.getAmount() - can);
                p.getInventory().setItem(i, is);
            } else {
                p.getInventory().setItem(i, null);
            }
            needed -= can;
        }
        Economy econ = plugin.getEconomyManager().getEconomy();
        if (econ != null) econ.depositPlayer(p, totalPrice);
        plugin.getDataBase().addPlayerEarnings(p.getUniqueId(), totalPrice);
        checkAndUpdateLevel(p);
        String raw = plugin.getConfigManager().getConfig().getString("message.successfully-buyer");
        raw = raw.replace("%prince%", String.format("%.2f", totalPrice));
        p.sendMessage(BuyerPlaceholder.apply(raw, p, entry, amount));
    }

    private void playMenuOpenSound(Player player) {
        try {
            Sound s = Sound.valueOf(plugin.getConfigManager().getConfig().getString("sound.sound_open_menu"));
            player.playSound(player.getLocation(), s, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }

    private void playNoItemSound(Player player) {
        try {
            Sound s = Sound.valueOf(plugin.getConfigManager().getConfig().getString("sound.no-item-sound"));
            player.playSound(player.getLocation(), s, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }

    private void checkAndUpdateLevel(Player p) {
        com.erydevs.levels.PlayerLevel playerLevel = plugin.getDataBase().getPlayerData(p.getUniqueId());
        int newLevel = plugin.getLevelConfig().getLevelByMoney(playerLevel.getTotalEarned());
        if (newLevel > playerLevel.getCurrentLevel()) {
            playerLevel.setCurrentLevel(newLevel);
            plugin.getDataBase().savePlayerData(playerLevel);
            String msg = plugin.getConfigManager().getConfig().getString("message.level-up");
            msg = msg.replace("%new_level%", String.valueOf(newLevel));
            p.sendMessage(com.erydevs.utils.HexUtils.colorize(msg));
            try {
                Sound s = Sound.valueOf(plugin.getConfigManager().getConfig().getString("sound.autobuyer-sound"));
                p.playSound(p.getLocation(), s, 1.0f, 1.0f);
            } catch (Exception ignored) {}
        }
    }

}