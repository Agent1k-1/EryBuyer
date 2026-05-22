package com.erydevs.listeners;

import com.erydevs.EryBuyer;
import com.erydevs.action.Actions;
import com.erydevs.gui.Entry;
import com.erydevs.gui.BuyerSite;
import com.erydevs.levels.PlayerLevel;
import com.erydevs.papi.PlaceholderAPIHook;
import com.erydevs.utils.sound.Sounds;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;
import java.util.stream.Collectors;

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

        List<String> acts = plugin.getBuyerGUI().getActions(title, slot);
        if (acts != null && !acts.isEmpty()) {
            Actions.dispatch(plugin, p, acts);
            return;
        }

        String menuName = plugin.getBuyerGUI().getMenuNameByTitle(title);
        org.bukkit.configuration.file.FileConfiguration menuCfg = plugin.getMenuRegistry().getMenuConfig(menuName);

        int exitSlot = plugin.getBuyerGUI().getExitSlot(title, menuCfg);
        if (exitSlot >= 0 && slot == exitSlot) {
            p.closeInventory();
            return;
        }

        int autobuyerSlot = plugin.getBuyerGUI().getAutobuyerSlot(title, menuCfg);
        if (autobuyerSlot >= 0 && slot == autobuyerSlot) {
            plugin.getAutoBuyerManager().toggleAutobuyer(p);
            List<String> lines = plugin.getConfigManager().getMessageAutoBuyerStatus().stream()
                    .map(line -> PlaceholderAPIHook.apply(line, p))
                    .collect(Collectors.toList());
            Actions.dispatch(plugin, p, lines);
            p.openInventory(plugin.getBuyerGUI().createInventory(p, menuName));
            sounds.playMenuOpenSound(p);
            return;
        }

        if (plugin.getAutoBuyerManager().isAutobuyerEnabled(p)) return;

        Entry entry = plugin.getBuyerGUI().getEntry(title, slot);
        if (entry == null) return;
        if (entry.priceX1 <= 0) return;

        BuyerSite.ClickType clickType;
        if (e.isShiftClick() && e.isLeftClick()) {
            clickType = BuyerSite.ClickType.SHIFT_LEFT;
        } else if (e.isLeftClick()) {
            clickType = BuyerSite.ClickType.LEFT;
        } else {
            clickType = BuyerSite.ClickType.RIGHT;
        }

        if (clickType == BuyerSite.ClickType.LEFT) {
            processSale(p, entry, 1, entry.priceX1);
            return;
        }
        if (clickType == BuyerSite.ClickType.RIGHT) {
            int totalCount = countItemsInInventory(p, entry.material);
            if (totalCount == 0) {
                List<String> lines = plugin.getConfigManager().getMessageNoItem().stream()
                        .map(line -> PlaceholderAPIHook.apply(line, p, entry, 64))
                        .collect(Collectors.toList());
                Actions.dispatch(plugin, p, lines);
                sounds.playNoItemSound(p);
                return;
            }
            if (totalCount < 64) return;
            processSale(p, entry, 64, entry.priceX1);
            return;
        }
        if (clickType == BuyerSite.ClickType.SHIFT_LEFT) {
            int totalCount = countItemsInInventory(p, entry.material);
            if (totalCount == 0) {
                List<String> lines = plugin.getConfigManager().getMessageNoItem().stream()
                        .map(line -> PlaceholderAPIHook.apply(line, p, entry, totalCount))
                        .collect(Collectors.toList());
                Actions.dispatch(plugin, p, lines);
                sounds.playNoItemSound(p);
                return;
            }
            processSale(p, entry, totalCount, entry.priceX1);
        }
    }

    private int countItemsInInventory(Player p, org.bukkit.Material material) {
        int total = 0;
        for (ItemStack is : p.getInventory().getContents()) {
            if (is != null && is.getType() == material) total += is.getAmount();
        }
        return total;
    }

    private int removeItemsFromInventory(Player p, Entry entry, int amountNeeded) {
        int removed = 0;
        ItemStack[] contents = p.getInventory().getContents();
        for (int i = 0; i < contents.length && removed < amountNeeded; i++) {
            ItemStack is = contents[i];
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
            List<String> lines = plugin.getConfigManager().getMessageNoItem().stream()
                    .map(line -> PlaceholderAPIHook.apply(line, p, entry, requestedAmount))
                    .collect(Collectors.toList());
            Actions.dispatch(plugin, p, lines);
            sounds.playNoItemSound(p);
            return;
        }

        PlayerLevel playerLevel = plugin.getDataBase().getPlayerData(p.getUniqueId());
        double basePrice = unitPrice * actualAmount;
        double multiplier = 1.0 + plugin.getLevelConfig().getMultiplierByLevel(playerLevel.getCurrentLevel());
        double totalPrice = basePrice * multiplier;
        Economy econ = plugin.getEconomyManager().getEconomy();
        if (econ != null) econ.depositPlayer(p, totalPrice);

        int maxLevel = plugin.getLevelConfig().getMaxLevel();
        if (playerLevel.getCurrentLevel() < maxLevel) {
            playerLevel.addEarnings(basePrice);
            checkAndUpdateLevel(p, playerLevel);
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> plugin.getDataBase().flushPlayerAsync(playerLevel));

        List<String> lines = plugin.getConfigManager().getMessageSuccessfullyBuyer().stream()
                .map(line -> PlaceholderAPIHook.apply(line, p, entry, actualAmount, totalPrice))
                .collect(Collectors.toList());
        Actions.dispatch(plugin, p, lines);
    }

    private void checkAndUpdateLevel(Player p, PlayerLevel playerLevel) {
        int maxLevel = plugin.getLevelConfig().getMaxLevel();
        double totalEarned = playerLevel.getTotalEarned();
        boolean leveled = false;

        while (playerLevel.getCurrentLevel() < maxLevel) {
            int nextLevel = playerLevel.getCurrentLevel() + 1;
            if (plugin.getLevelConfig().getRequiredMoneyForLevel(nextLevel) <= totalEarned) {
                playerLevel.setCurrentLevel(nextLevel);
                leveled = true;
                List<String> lines = plugin.getConfigManager().getMessageLevelUp().stream()
                        .map(line -> PlaceholderAPIHook.applyLevelUp(line, p, nextLevel))
                        .collect(Collectors.toList());
                Actions.dispatch(plugin, p, lines);
            } else {
                break;
            }
        }

        if (leveled) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                    () -> plugin.getDataBase().savePlayerData(playerLevel));
        }
    }
}
