package com.erydevs.autobuyer;

import com.erydevs.EryBuyer;
import com.erydevs.action.Actions;
import com.erydevs.gui.entry.Entry;
import com.erydevs.levels.PlayerLevel;
import com.erydevs.papi.PlaceholderAPIHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class AutoBuyerManager {

    private final EryBuyer plugin;
    private final Map<UUID, Boolean> autobuyers = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastSellTime = new ConcurrentHashMap<>();

    private int tickTaskId = -1;
    private int topPlayersTaskId = -1;

    public AutoBuyerManager(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
        startTickTask();
        startTopPlayersUpdateTask();
    }

    private void startTickTask() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        tickTaskId = scheduler.scheduleSyncRepeatingTask(plugin, this::processOnlinePlayers, 0L, 40L);
    }

    private void startTopPlayersUpdateTask() {
        BukkitScheduler scheduler = plugin.getServer().getScheduler();
        long interval = plugin.getConfigManager().getBuyerTopUpdateInterval() * 20L;
        topPlayersTaskId = scheduler.scheduleSyncRepeatingTask(plugin, () ->
                scheduler.runTaskAsynchronously(plugin, this::refreshTopPlayers), interval, interval);
    }

    private void refreshTopPlayers() {
        plugin.getDataBase().refreshTopPlayersCache(plugin.getConfigManager().getBuyerTopUpdateMoney());
    }

    private void processOnlinePlayers() {
        Collection<Entry> entries = plugin.getBuyerGUI().getBuyableEntries();
        if (entries.isEmpty()) return;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            if (isAutobuyerEnabled(player)) {
                processPlayer(player, entries);
            }
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
        autobuyers.put(uuid, enabled);

        if (enabled) {
            lastSellTime.put(uuid, System.currentTimeMillis());
            if (plugin.getConfigManager().isBossbarEnabled() && plugin.getBossBarManager() != null) {
                plugin.getBossBarManager().createBossBar(player);
            }
        } else {
            lastSellTime.remove(uuid);
            if (plugin.getBossBarManager() != null) {
                plugin.getBossBarManager().removeBossBar(player);
            }
        }
    }

    public boolean isAutobuyerEnabled(@NotNull Player player) {
        return autobuyers.getOrDefault(player.getUniqueId(), false);
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
        PlayerLevel playerLevel = plugin.getDataBase().getPlayerData(player.getUniqueId());

        double basePrice = entry.priceX1 * amount;
        double multiplier = 1.0 + plugin.getLevelConfig().getMultiplierByLevel(playerLevel.getCurrentLevel());
        double totalPrice = basePrice * multiplier;

        Economy economy = plugin.getEconomyManager().getEconomy();
        if (economy != null) {
            economy.depositPlayer(player, totalPrice);
        }

        int maxLevel = plugin.getLevelConfig().getMaxLevel();
        if (playerLevel.getCurrentLevel() < maxLevel) {
            playerLevel.addEarnings(basePrice);
            tryLevelUp(player, playerLevel);
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> plugin.getDataBase().flushPlayerAsync(playerLevel));

        List<String> lines = plugin.getMessagesConfig().getMessageAutoBuyer().stream()
                .map(line -> PlaceholderAPIHook.apply(line, player, entry, amount, totalPrice))
                .collect(Collectors.toList());
        Actions.dispatch(plugin, player, lines);

        playSellSound(player);
    }

    private void tryLevelUp(@NotNull Player player, @NotNull PlayerLevel playerLevel) {
        int maxLevel = plugin.getLevelConfig().getMaxLevel();
        double totalEarned = playerLevel.getTotalEarned();
        boolean leveledUp = false;

        while (playerLevel.getCurrentLevel() < maxLevel) {
            int nextLevel = playerLevel.getCurrentLevel() + 1;
            if (plugin.getLevelConfig().getRequiredMoneyForLevel(nextLevel) > totalEarned) break;

            playerLevel.setCurrentLevel(nextLevel);
            leveledUp = true;

            List<String> lines = plugin.getMessagesConfig().getMessageLevelUp().stream()
                    .map(line -> PlaceholderAPIHook.applyLevelUp(line, player, nextLevel))
                    .collect(Collectors.toList());
            Actions.dispatch(plugin, player, lines);
        }

        if (leveledUp) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                    () -> plugin.getDataBase().savePlayerData(playerLevel));
        }
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
        plugin.getServer().getScheduler().cancelTask(tickTaskId);
        plugin.getServer().getScheduler().cancelTask(topPlayersTaskId);

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            removePlayer(player);
        }

        autobuyers.clear();
        lastSellTime.clear();
    }
}