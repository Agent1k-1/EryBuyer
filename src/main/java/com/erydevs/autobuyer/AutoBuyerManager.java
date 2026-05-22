package com.erydevs.autobuyer;

import com.erydevs.EryBuyer;
import com.erydevs.action.Actions;
import com.erydevs.gui.Entry;
import com.erydevs.levels.PlayerLevel;
import com.erydevs.papi.PlaceholderAPIHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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
    private int taskId = -1;
    private int topTaskId = -1;

    public AutoBuyerManager(EryBuyer plugin) {
        this.plugin = plugin;
        startTickTask();
        startTopPlayersUpdateTask();
    }

    private void startTickTask() {
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::processAllPlayers, 0, 40);
    }

    private void startTopPlayersUpdateTask() {
        long interval = plugin.getConfigManager().getBuyerTopUpdateInterval() * 20L;
        topTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,
                () -> plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                        () -> plugin.getDataBase().refreshTopPlayersCache(plugin.getConfigManager().getBuyerTopUpdateMoney())),
                interval, interval);
    }

    private void processAllPlayers() {
        Collection<Entry> entries = plugin.getBuyerGUI().getBuyableEntries();
        if (entries.isEmpty()) return;
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (isAutobuyerEnabled(p)) {
                processPlayerInventory(p, entries);
            }
        }
    }

    public void toggleAutobuyer(Player player) {
        setAutobuyer(player, !isAutobuyerEnabled(player));
    }

    public void setAutobuyer(Player player, boolean enabled) {
        UUID id = player.getUniqueId();
        autobuyers.put(id, enabled);
        boolean bossbarEnabled = plugin.getConfigManager().isBossbarEnabled();
        if (enabled) {
            lastSellTime.put(id, System.currentTimeMillis());
            if (bossbarEnabled && plugin.getBossBarManager() != null) plugin.getBossBarManager().createBossBar(player);
        } else {
            lastSellTime.remove(id);
            if (plugin.getBossBarManager() != null) plugin.getBossBarManager().removeBossBar(player);
        }
    }

    public boolean isAutobuyerEnabled(Player player) {
        return autobuyers.getOrDefault(player.getUniqueId(), false);
    }

    public void removePlayer(Player player) {
        UUID id = player.getUniqueId();
        autobuyers.remove(id);
        lastSellTime.remove(id);
        if (plugin.getBossBarManager() != null) plugin.getBossBarManager().removeBossBar(player);
        plugin.getDataBase().evictPlayer(id);
    }

    public void processPlayerInventory(Player p, Collection<Entry> entries) {
        UUID id = p.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long lastTime = lastSellTime.getOrDefault(id, 0L);
        if (currentTime - lastTime < getAutobuyerDelay()) return;

        ItemStack[] contents = p.getInventory().getContents();

        for (Entry entry : entries) {
            if (entry == null || entry.material == null || entry.priceX1 <= 0) continue;

            int total = 0;
            for (ItemStack is : contents) {
                if (is != null && is.getType() == entry.material) total += is.getAmount();
            }

            if (total > 0) {
                for (int i = 0; i < contents.length; i++) {
                    ItemStack is = contents[i];
                    if (is != null && is.getType() == entry.material) {
                        p.getInventory().setItem(i, null);
                        contents[i] = null;
                    }
                }
                lastSellTime.put(id, currentTime);
                depositAndNotify(p, entry, total);
                return;
            }
        }
    }

    private long getAutobuyerDelay() {
        return plugin.getConfigManager().getAutobuyerTime() * 50L;
    }

    private void depositAndNotify(Player p, Entry entry, int amount) {
        PlayerLevel playerLevel = plugin.getDataBase().getPlayerData(p.getUniqueId());
        double basePrice = entry.priceX1 * amount;
        double multiplier = 1.0 + plugin.getLevelConfig().getMultiplierByLevel(playerLevel.getCurrentLevel());
        double total = basePrice * multiplier;

        Economy econ = plugin.getEconomyManager().getEconomy();
        if (econ != null) econ.depositPlayer(p, total);

        int maxLevel = plugin.getLevelConfig().getMaxLevel();
        if (playerLevel.getCurrentLevel() < maxLevel) {
            playerLevel.addEarnings(basePrice);
            checkAndUpdateLevel(p, playerLevel);
        }

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> plugin.getDataBase().flushPlayerAsync(playerLevel));

        List<String> lines = plugin.getConfigManager().getMessageAutoBuyer().stream()
                .map(line -> PlaceholderAPIHook.apply(line, p, entry, amount, total))
                .collect(Collectors.toList());
        Actions.dispatch(plugin, p, lines);
        playSound(p);
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

    private void playSound(Player p) {
        if (!plugin.getConfigManager().isSoundAutobuyerEnabled()) return;
        try {
            Sound s = Sound.valueOf(plugin.getConfigManager().getSoundAutobuyer());
            p.playSound(p.getLocation(), s, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }

    public void shutdown() {
        plugin.getServer().getScheduler().cancelTask(taskId);
        plugin.getServer().getScheduler().cancelTask(topTaskId);
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            removePlayer(p);
        }
        autobuyers.clear();
        lastSellTime.clear();
    }
}
