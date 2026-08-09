package com.erydevs.buyer.autobuyer;

import com.erydevs.EryBuyer;
import com.erydevs.action.ActionType;
import com.erydevs.buyer.boosters.PlayerBooster;
import com.erydevs.gui.entry.Entry;
import com.erydevs.papi.PlaceholderAPIHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AutoBuyerManager {

    private final EryBuyer plugin;
    private final Set<UUID> autobuyers = ConcurrentHashMap.newKeySet();
    private final Map<UUID, Long> lastSellTime = new ConcurrentHashMap<>();

    public AutoBuyerManager(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
        startTickTask();
        startTopPointsUpdateTask();
    }

    private void startTickTask() {
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::processOnlinePlayers, 0L, 40L);
    }

    private void startTopPointsUpdateTask() {
        long intervalTicks = Math.max(20L, plugin.getConfigManager().getBuyerTopUpdateInterval() * 20L);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin,
                () -> plugin.getDataBase().refreshTopPointsCache(),
                intervalTicks, intervalTicks);
    }

    private void processOnlinePlayers() {
        if (autobuyers.isEmpty()) return;

        Collection<Entry> entries = plugin.getBuyerGUI().getBuyableEntries();
        if (entries.isEmpty()) return;

        for (UUID uuid : autobuyers) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;
            processPlayer(player, entries);
        }
    }

    private void processPlayer(@NotNull Player player, @NotNull Collection<Entry> entries) {
        UUID uuid = player.getUniqueId();
        long now = System.currentTimeMillis();
        long lastSell = lastSellTime.getOrDefault(uuid, 0L);
        if (now - lastSell < getAutobuyerDelay()) return;

        boolean sold = false;

        for (Entry entry : entries) {
            if (!isSellable(entry)) continue;

            int amount = countItems(player, entry);
            if (amount == 0) continue;

            removeItems(player, entry);
            sell(player, entry, amount);
            sold = true;
        }

        if (sold) {
            lastSellTime.put(uuid, now);
        }
    }

    private boolean isSellable(@Nullable Entry entry) {
        return entry != null && entry.material != null && entry.priceX1 > 0;
    }

    private long getAutobuyerDelay() {
        return plugin.getConfigManager().getAutobuyerTime() * 50L;
    }

    public void toggleAutobuyer(@NotNull Player player) {
        setAutobuyer(player, !isAutobuyerEnabled(player));
    }

    public void setAutobuyer(@NotNull Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();

        if (enabled) {
            autobuyers.add(uuid);
            lastSellTime.put(uuid, System.currentTimeMillis());
            if (plugin.getConfigManager().isBossbarEnabled() && plugin.getBossBarManager() != null) {
                plugin.getBossBarManager().createBossBar(player);
            }
        } else {
            autobuyers.remove(uuid);
            lastSellTime.remove(uuid);
            if (plugin.getBossBarManager() != null) {
                plugin.getBossBarManager().removeBossBar(player);
            }
        }
    }

    public boolean isAutobuyerEnabled(@NotNull Player player) {
        return autobuyers.contains(player.getUniqueId());
    }

    public void removePlayer(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        autobuyers.remove(uuid);
        lastSellTime.remove(uuid);

        if (plugin.getBossBarManager() != null) {
            plugin.getBossBarManager().removeBossBar(player);
        }

        plugin.getDataBase().evictPlayer(uuid);
    }

    private int countItems(@NotNull Player player, @NotNull Entry entry) {
        int total = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == entry.material) {
                total += item.getAmount();
            }
        }
        return total;
    }

    private void removeItems(@NotNull Player player, @NotNull Entry entry) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item != null && item.getType() == entry.material) {
                player.getInventory().setItem(i, null);
            }
        }
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
                .map(line -> PlaceholderAPIHook.apply(line, player, entry, amount, totalPrice))
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

    public void shutdown() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            removePlayer(player);
        }

        autobuyers.clear();
        lastSellTime.clear();
    }
}
