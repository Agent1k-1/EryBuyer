package com.erydevs.autobuyer;

import com.erydevs.EryBuyer;
import com.erydevs.gui.Entry;
import com.erydevs.placeholders.PlaceholderAPIHook;
import com.erydevs.levels.PlayerLevel;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class AutoBuyerManager {

    private final EryBuyer plugin;
    private final Map<UUID, Boolean> autobuyers = new ConcurrentHashMap<>();
    private final Map<UUID, Long> lastSellTime = new ConcurrentHashMap<>();
    private int taskId = -1;
    private static final long TASK_INTERVAL = 40L;

    public AutoBuyerManager(EryBuyer plugin) {
        this.plugin = plugin;
        startTickTask();
    }

    private void startTickTask() {
        taskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, 
            this::processAllPlayers, 1, TASK_INTERVAL);
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
        if (player == null) return;
        
        UUID id = player.getUniqueId();
        autobuyers.put(id, enabled);
        boolean bossbarEnabled = plugin.getConfigManager().getConfig().getBoolean("bossbar-settings.bossbar", true);
        if (enabled) {
            lastSellTime.put(id, System.currentTimeMillis());
            if (bossbarEnabled && plugin.getBossBarManager() != null) {
                plugin.getBossBarManager().createBossBar(player);
            }
        } else {
            lastSellTime.remove(id);
            if (plugin.getBossBarManager() != null) {
                plugin.getBossBarManager().removeBossBar(player);
            }
        }
    }

    public boolean isAutobuyerEnabled(Player player) {
        if (player == null) return false;
        return autobuyers.getOrDefault(player.getUniqueId(), false);
    }

    public void removePlayer(Player player) {
        if (player == null) return;
        
        UUID id = player.getUniqueId();
        autobuyers.remove(id);
        lastSellTime.remove(id);
        if (plugin.getBossBarManager() != null) {
            plugin.getBossBarManager().removeBossBar(player);
        }
    }

    public void processPlayerInventory(Player p) {
        if (p == null || !p.isOnline()) return;
        
        UUID id = p.getUniqueId();
        long currentTime = System.currentTimeMillis();
        long delay = getAutobuyerDelay();
        
        long lastTime = lastSellTime.getOrDefault(id, 0L);
        if (currentTime - lastTime < delay) {
            return;
        }
        
        for (Entry entry : plugin.getBuyerGUI().getAllEntries()) {
            if (entry == null || entry.material == null || entry.priceX1 <= 0) {
                continue;
            }
            
            int stackAmount = calculateItemAmount(p, entry);
            if (stackAmount > 0) {
                processSale(p, entry, stackAmount);
                lastSellTime.put(id, currentTime);
                return;
            }
        }
    }

    private int calculateItemAmount(Player p, Entry entry) {
        int total = 0;
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack is = p.getInventory().getItem(i);
            if (is != null && is.getType() == entry.material) {
                total += is.getAmount();
            }
        }
        return total;
    }

    private void processSale(Player p, Entry entry, int amount) {
        if (p == null || entry == null) return;
        
        // Single DB query
        PlayerLevel playerLevel = plugin.getDataBase().getPlayerData(p.getUniqueId());
        if (playerLevel == null) return;
        
        double multiplier = 1.0 + plugin.getLevelConfig().getMultiplierByLevel(playerLevel.getCurrentLevel());
        double total = entry.priceX1 * amount * multiplier;
        
        removeItemsFromInventory(p, entry);
        
        Economy econ = plugin.getEconomyManager().getEconomy();
        if (econ != null) {
            econ.depositPlayer(p, total);
        }
        
        int maxLevel = plugin.getLevelConfig().getMaxLevel();
        if (playerLevel.getCurrentLevel() < maxLevel) {
            plugin.getDataBase().addPlayerEarnings(p.getUniqueId(), total);
            checkAndUpdateLevel(p, playerLevel);
        }
        
        String msg = plugin.getConfigManager().getConfig().getString("message.auto-buyer");
        p.sendMessage(PlaceholderAPIHook.apply(msg, p, entry, amount));
        playSaleSound(p);
    }

    private void removeItemsFromInventory(Player p, Entry entry) {
        if (p == null || entry == null || entry.material == null) return;
        
        for (int i = 0; i < p.getInventory().getSize(); i++) {
            ItemStack is = p.getInventory().getItem(i);
            if (is != null && is.getType() == entry.material) {
                p.getInventory().setItem(i, null);
            }
        }
    }

    private long getAutobuyerDelay() {
        long configValue = plugin.getConfigManager().getConfig().getLong("bossbar-settings.autobuyer-time", 40);
        return configValue * 50;
    }

    private void checkAndUpdateLevel(Player p, PlayerLevel playerLevel) {
        if (p == null || playerLevel == null) return;
        
        int newLevel = plugin.getLevelConfig().getLevelByMoney(playerLevel.getTotalEarned());
        if (newLevel > playerLevel.getCurrentLevel()) {
            playerLevel.setCurrentLevel(newLevel);
            plugin.getDataBase().savePlayerData(playerLevel);
            String msg = plugin.getConfigManager().getConfig().getString("message.level-up");
            p.sendMessage(PlaceholderAPIHook.apply(msg, p));
            playLevelUpSound(p);
        }
    }

    private void playSaleSound(Player p) {
        if (p == null || !p.isOnline()) return;
        
        try {
            String soundName = plugin.getConfigManager().getConfig().getString("sound.autobuyer-sound");
            if (soundName != null && !soundName.isEmpty()) {
                Sound s = Sound.valueOf(soundName);
                p.playSound(p.getLocation(), s, 1.0f, 1.0f);
            }
        } catch (Exception ignored) {
        }
    }
    
    private void playLevelUpSound(Player p) {
        if (p == null || !p.isOnline()) return;
        
        try {
            String soundName = plugin.getConfigManager().getConfig().getString("sound.level-up-sound");
            if (soundName != null && !soundName.isEmpty()) {
                Sound s = Sound.valueOf(soundName);
                p.playSound(p.getLocation(), s, 1.0f, 1.0f);
            }
        } catch (Exception ignored) {
        }
    }

    public void shutdown() {
        if (taskId != -1) {
            plugin.getServer().getScheduler().cancelTask(taskId);
            taskId = -1;
        }
        autobuyers.clear();
        lastSellTime.clear();
    }
}