package com.erydevs.buyer.autobuyer.task;

import com.erydevs.EryBuyer;
import com.erydevs.action.ActionType;
import com.erydevs.buyer.autobuyer.AutoBuyerManager;
import com.erydevs.buyer.boosters.PlayerBooster;
import com.erydevs.gui.entry.Entry;
import com.erydevs.papi.Placeholders;
import com.erydevs.utils.inventory.InventoryUtils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AutoBuyerTask {

    private final EryBuyer plugin;
    private final AutoBuyerManager manager;
    private final Set<UUID> dirty = ConcurrentHashMap.newKeySet();

    private BukkitTask task;

    public AutoBuyerTask(@NotNull EryBuyer plugin, @NotNull AutoBuyerManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void start() {
        if (task != null) return;
        long period = Math.max(1L, plugin.getConfigManager().getAutobuyerTime());
        task = plugin.getServer().getScheduler().runTaskTimer(plugin, this::processDirtyPlayers, period, period);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        dirty.clear();
    }

    public void markDirty(@NotNull UUID uuid) {
        dirty.add(uuid);
    }

    public void forget(@NotNull UUID uuid) {
        dirty.remove(uuid);
    }

    private void processDirtyPlayers() {
        if (dirty.isEmpty()) return;

        Collection<Entry> entries = plugin.getBuyerGUI().getBuyableEntries();
        if (entries.isEmpty()) return;

        Iterator<UUID> it = dirty.iterator();
        while (it.hasNext()) {
            UUID uuid = it.next();
            it.remove();

            Player player = plugin.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            processPlayer(player, entries);
        }
    }

    private void processPlayer(@NotNull Player player, @NotNull Collection<Entry> entries) {
        for (Entry entry : entries) {
            if (!isSellable(entry)) continue;

            int amount = InventoryUtils.remove(player, entry.material, InventoryUtils.count(player, entry.material));
            if (amount == 0) continue;

            sell(player, entry, amount);
        }
    }

    private boolean isSellable(@Nullable Entry entry) {
        return entry != null && entry.material != null && entry.priceX1 > 0;
    }

    private void sell(@NotNull Player player, @NotNull Entry entry, int amount) {
        PlayerBooster booster = plugin.getDataBase().getPlayerData(player.getUniqueId());

        double basePrice = entry.priceX1 * amount;
        double multiplier = plugin.getBoosterManager().getMoneyMultiplier(booster);
        double totalPrice = basePrice * multiplier;

        Economy economy = plugin.getEconomyManager().getEconomy();
        if (economy != null) {
            economy.depositPlayer(player, totalPrice);
        }

        long pointsEarned = (long) entry.pointsX1 * amount;
        plugin.getBoosterManager().addPointsAndCheckLevelUp(player, booster, pointsEarned);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> plugin.getDataBase().save(booster));

        List<String> lines = plugin.getMessagesConfig().getMessageAutoBuyer().stream()
                .map(line -> Placeholders.apply(line, player, entry, amount, totalPrice))
                .collect(Collectors.toList());
        ActionType.dispatchAll(plugin, player, lines);

        playSellSound(player);
    }

    private void playSellSound(@NotNull Player player) {
        if (!plugin.getConfigManager().isSoundAutobuyerEnabled()) return;

        try {
            Sound sound = Sound.valueOf(plugin.getConfigManager().getSoundAutobuyer());
            float volume = plugin.getConfigManager().getSoundAutobuyerVolume();
            float pitch = plugin.getConfigManager().getSoundAutobuyerPitch();
            player.playSound(player.getLocation(), sound, volume, pitch);
        } catch (IllegalArgumentException ignored) {
        }
    }
}
