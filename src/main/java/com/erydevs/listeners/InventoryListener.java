package com.erydevs.listeners;

import com.erydevs.EryBuyer;
import com.erydevs.action.ActionType;
import com.erydevs.buyer.boosters.PlayerBooster;
import com.erydevs.gui.click.ClickType;
import com.erydevs.gui.entry.Entry;
import com.erydevs.papi.Placeholders;
import com.erydevs.utils.inventory.InventoryUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class InventoryListener implements Listener {

    private final EryBuyer plugin;

    public InventoryListener(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(@NotNull InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (!plugin.getBuyerGUI().isManagedTitle(title)) return;
        e.setCancelled(true);

        int slot = e.getRawSlot();
        if (slot < 0) return;
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();

        List<String> acts = plugin.getBuyerGUI().getActions(title, slot);
        if (acts != null && !acts.isEmpty()) {
            ActionType.dispatchAll(plugin, p,acts);
            return;
        }

        boolean bestTitle = plugin.getBestManager() != null && plugin.getBestManager().isBestTitle(title);
        if (bestTitle && plugin.getBestManager().getActiveBySlot(slot) != null) {
            if (plugin.getAutoBuyerManager().isAutobuyerEnabled(p)) return;
            ClickType bestClick = resolveClickType(e);
            plugin.getBestManager().handleClick(p, slot, bestClick);
            return;
        }

        if (plugin.getAutoBuyerManager().isAutobuyerEnabled(p)) return;

        Entry entry = plugin.getBuyerGUI().getEntry(title, slot);
        if (entry == null || entry.priceX1 <= 0) return;

        ClickType clickType = resolveClickType(e);

        if (clickType == ClickType.LEFT) {
            processSale(p, entry, 1, entry.priceX1);
            return;
        }
        if (clickType == ClickType.RIGHT) {
            int totalCount = InventoryUtils.count(p, entry.material);
            if (totalCount == 0) {
                List<String> lines = plugin.getMessagesConfig().getMessageNoItem().stream()
                        .map(line -> Placeholders.apply(line, p, entry, 64))
                        .collect(Collectors.toList());
                ActionType.dispatchAll(plugin, p,lines);
                playNoItemSound(p);
                return;
            }
            if (totalCount < 64) return;
            processSale(p, entry, 64, entry.priceX1);
            return;
        }
        if (clickType == ClickType.SHIFT_LEFT || clickType == ClickType.SHIFT_RIGHT) {
            int totalCount = InventoryUtils.count(p, entry.material);
            if (totalCount == 0) {
                List<String> lines = plugin.getMessagesConfig().getMessageNoItem().stream()
                        .map(line -> Placeholders.apply(line, p, entry, totalCount))
                        .collect(Collectors.toList());
                ActionType.dispatchAll(plugin, p,lines);
                playNoItemSound(p);
                return;
            }
            processSale(p, entry, totalCount, entry.priceX1);
        }
    }

    @NotNull
    private ClickType resolveClickType(@NotNull InventoryClickEvent e) {
        if (e.isShiftClick() && e.isLeftClick()) return ClickType.SHIFT_LEFT;
        if (e.isShiftClick() && e.isRightClick()) return ClickType.SHIFT_RIGHT;
        if (e.isLeftClick()) return ClickType.LEFT;
        return ClickType.RIGHT;
    }

    private void processSale(@NotNull Player p, @NotNull Entry entry, int requestedAmount, double unitPrice) {
        int actualAmount = InventoryUtils.remove(p, entry.material, requestedAmount);

        if (actualAmount == 0) {
            List<String> lines = plugin.getMessagesConfig().getMessageNoItem().stream()
                    .map(line -> Placeholders.apply(line, p, entry, requestedAmount))
                    .collect(Collectors.toList());
            ActionType.dispatchAll(plugin, p,lines);
            playNoItemSound(p);
            return;
        }

        PlayerBooster booster = plugin.getDataBase().getPlayerData(p.getUniqueId());
        double basePrice = unitPrice * actualAmount;
        double multiplier = plugin.getBoosterManager().getMoneyMultiplier(booster);
        double totalPrice = basePrice * multiplier;

        Economy econ = plugin.getEconomyManager().getEconomy();
        if (econ != null) econ.depositPlayer(p, totalPrice);

        long pointsEarned = (long) entry.pointsX1 * actualAmount;
        plugin.getBoosterManager().addPointsAndCheckLevelUp(p, booster, pointsEarned);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> plugin.getDataBase().save(booster));

        List<String> lines = plugin.getMessagesConfig().getMessageSuccessfullyBuyer().stream()
                .map(line -> Placeholders.apply(line, p, entry, actualAmount, totalPrice))
                .collect(Collectors.toList());
        ActionType.dispatchAll(plugin, p,lines);
        playExchangeSound(p);
    }

    private void playSound(@NotNull Player p, @NotNull String soundName, float volume, float pitch, boolean enabled) {
        if (!enabled) return;
        try {
            Sound sound = Sound.valueOf(soundName);
            p.playSound(p.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {
        }
    }

    private void playNoItemSound(@NotNull Player p) {
        playSound(p, plugin.getConfigManager().getSoundNoItem(),
                plugin.getConfigManager().getSoundNoItemVolume(),
                plugin.getConfigManager().getSoundNoItemPitch(),
                plugin.getConfigManager().isSoundNoItemEnabled());
    }

    private void playExchangeSound(@NotNull Player p) {
        playSound(p, plugin.getConfigManager().getSoundExchange(),
                plugin.getConfigManager().getSoundExchangeVolume(),
                plugin.getConfigManager().getSoundExchangePitch(),
                plugin.getConfigManager().isSoundExchangeEnabled());
    }
}