package com.erydevs.bossbar;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import com.erydevs.EryBuyer;
import com.erydevs.placeholders.PlaceholderAPIHook;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BossBarManager {

    private final EryBuyer plugin;
    private final Map<UUID, BossBar> bossbars = new ConcurrentHashMap<>();
    private int updateTaskId = -1;
    private static final long BOSSBAR_UPDATE_INTERVAL = 20L;

    public BossBarManager(EryBuyer plugin) {
        this.plugin = plugin;
        startBossBarUpdateTask();
    }

    private void startBossBarUpdateTask() {
        updateTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin,
                this::updateAllBossBars, 1, BOSSBAR_UPDATE_INTERVAL);
    }

    private void updateAllBossBars() {
        for (UUID uuid : bossbars.keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null && player.isOnline()) {
                updateBossBar(player);
            }
        }
    }

    public void createBossBar(Player player) {
        if (player == null) return;
        UUID id = player.getUniqueId();
        
        if (bossbars.containsKey(id)) {
            return;
        }
        
        try {
            String raw = plugin.getConfigManager().getConfig()
                    .getString("bossbar-settings.text");
            
            String text = raw;
            if (PlaceholderAPIHook.isAvailable()) {
                text = PlaceholderAPIHook.apply(raw, player);
            } else {
                text = com.erydevs.utils.HexUtils.colorize(raw);
            }
            
            if (text == null || text.isEmpty()) {
                text = "&fУ вас включён &cавто-скупщик";
            }
            
            BarColor color = parseBarColor(
                    plugin.getConfigManager().getConfig()
                            .getString("bossbar-settings.color")
            );
            
            BossBar bar = plugin.getServer().createBossBar(
                    com.erydevs.utils.HexUtils.colorize(text), 
                    color, 
                    BarStyle.SOLID
            );
            bar.addPlayer(player);
            bar.setVisible(true);
            bar.setProgress(1.0);
            bossbars.put(id, bar);
            
        } catch (Exception ignored) {
        }
    }
    
    private BarColor parseBarColor(String colorName) {
        if (colorName == null || colorName.isEmpty()) {
            return BarColor.RED;
        }
        
        try {
            return BarColor.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return BarColor.RED;
        }
    }

    public void updateBossBar(Player player) {
        if (player == null || !player.isOnline()) return;

        UUID id = player.getUniqueId();
        BossBar bar = bossbars.get(id);
        if (bar == null || !bar.getPlayers().contains(player)) return;

        try {
            String raw = plugin.getConfigManager().getConfig()
                    .getString("bossbar-settings.text");
            
            String text = raw;
            if (PlaceholderAPIHook.isAvailable()) {
                text = PlaceholderAPIHook.apply(raw, player);
            } else {
                text = com.erydevs.utils.HexUtils.colorize(raw);
            }
            
            if (text == null || text.isEmpty()) {
                text = "&fУ вас включён &cавто-скупщик";
            }
            
            bar.setTitle(com.erydevs.utils.HexUtils.colorize(text));
        } catch (Exception ignored) {
        }
    }

    public void removeBossBar(Player player) {
        if (player == null) return;
        
        UUID id = player.getUniqueId();
        BossBar bar = bossbars.remove(id);
        
        if (bar != null) {
            try {
                bar.removeAll();
                bar.setVisible(false);
            } catch (Exception ignored) {
            }
        }
    }

    public BossBar getBossBar(Player player) {
        if (player == null) return null;
        return bossbars.get(player.getUniqueId());
    }

    public void removeAllBossBars() {
        try {
            for (BossBar bar : bossbars.values()) {
                if (bar != null) {
                    bar.removeAll();
                    bar.setVisible(false);
                }
            }
        } catch (Exception ignored) {
        } finally {
            bossbars.clear();
        }
    }

    public void shutdown() {
        if (updateTaskId != -1) {
            plugin.getServer().getScheduler().cancelTask(updateTaskId);
            updateTaskId = -1;
        }
        removeAllBossBars();
    }
    
    public int getActiveBossBarCount() {
        return bossbars.size();
    }
}