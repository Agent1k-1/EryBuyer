package com.erydevs.autobuyer;

import com.erydevs.EryBuyer;
import com.erydevs.gui.Entry;
import com.erydevs.placeholders.BuyerPlaceholder;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AutoBuyerManager {

    private final EryBuyer plugin;
    private final Map<UUID, Boolean> autobuyers = new HashMap<>();
    private final Map<UUID, Long> lastSellTime = new HashMap<>();
    private int taskId = -1;

    public AutoBuyerManager(EryBuyer plugin) {
        this.plugin = plugin;
        startTickTask();
    }

    private void startTickTask() {
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::processAllPlayers, 1, 1);
    }

    private void processAllPlayers() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (isAutobuyerEnabled(p)) {
                processPlayerInventory(p);
            }
        }
    }

    public void toggleAutobuyer(Player player) {
        setAutobuyer(player, !isAutobuyerEnabled(player));
    }

    public void setAutobuyer(Player player, boolean enabled) {
        UUID id = player.getUniqueId();
        autobuyers.put(id, enabled);
        if (enabled) {
            lastSellTime.put(id, System.currentTimeMillis());
            if (plugin.getBossBarManager() != null) plugin.getBossBarManager().createBossBar(player);
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
    }

    public void processPlayerInventory(Player p) {
        UUID id = p.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long delay = getAutobueyerDelay();
        
        if (!lastSellTime.containsKey(id)) {
            lastSellTime.put(id, currentTime);
            return;
        }
        
        long lastTime = lastSellTime.get(id);
        if (currentTime - lastTime < delay) {
            return;
        }
        
        for (Entry entry : plugin.getBuyerGUI().getAllEntries()) {
            if (entry == null || entry.material == null || entry.priceX1 <= 0) continue;
            int stackAmount = removeItemsFromInventory(p, entry);
            if (stackAmount > 0) {
                lastSellTime.put(id, currentTime);
                depositAndNotify(p, entry, stackAmount);
                return;
            }
        }
    }

    private int removeItemsFromInventory(Player p, Entry entry) {
        int total = 0;
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack is = p.getInventory().getItem(i);
            if (is != null && is.getType() == entry.material) {
                total += is.getAmount();
                p.getInventory().setItem(i, null);
            }
        }
        return total;
    }

    private long getAutobueyerDelay() {
        long configValue = plugin.getConfigManager().getConfig().getLong("bossbar-settings.autobuyer-time", 40);
        return configValue * 50;
    }

    private void depositAndNotify(Player p, Entry entry, int amount) {
        com.erydevs.levels.PlayerLevel playerLevel = plugin.getDataBase().getPlayerData(p.getUniqueId());
        double multiplier = 1.0 + plugin.getLevelConfig().getMultiplierByLevel(playerLevel.getCurrentLevel());
        double total = entry.priceX1 * amount * multiplier;
        Economy econ = plugin.getEconomyManager().getEconomy();
        if (econ != null) econ.depositPlayer(p, total);
        plugin.getDataBase().addPlayerEarnings(p.getUniqueId(), total);
        checkAndUpdateLevel(p);
        String msg = plugin.getConfigManager().getConfig().getString("message.auto-buyer");
        p.sendMessage(BuyerPlaceholder.apply(msg, p, entry, amount));
        playSound(p);
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
                Sound s = Sound.valueOf(plugin.getConfigManager().getConfig().getString("autobuyer-sound"));
                p.playSound(p.getLocation(), s, 1.0f, 1.0f);
            } catch (Exception ignored) {}
        }
    }

    private void playSound(Player p) {
        try {
            Sound s = Sound.valueOf(plugin.getConfigManager().getConfig().getString("autobuyer-sound"));
            p.playSound(p.getLocation(), s, 1.0f, 1.0f);
        } catch (Exception ignored) {}
    }
}