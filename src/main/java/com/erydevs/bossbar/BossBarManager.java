package com.erydevs.bossbar;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import com.erydevs.EryBuyer;
import com.erydevs.placeholders.PlaceholderAPIHook;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarManager {

    private final EryBuyer plugin;
    private final Map<UUID, BossBar> bossbars = new ConcurrentHashMap<>();
    private int updateTaskId = -1;

    public BossBarManager(EryBuyer plugin) {
        this.plugin = plugin;
        startBossBarUpdateTask();
    }

    private void startBossBarUpdateTask() {
        updateTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::updateAllBossBars, 20, 20);
    }

    public void createBossBar(Player player) {
        UUID id = player.getUniqueId();
        if (bossbars.containsKey(id)) return;
        String raw = plugin.getConfigManager().getBossbarText();
        String text = PlaceholderAPIHook.apply(raw, player);
        BarColor color = BarColor.RED;
        try {
            String c = plugin.getConfigManager().getBossbarColor();
            color = BarColor.valueOf(c.toUpperCase());
        } catch (Exception ignored) {}
        BossBar bar = plugin.getServer().createBossBar(text, color, BarStyle.SOLID);
        bar.addPlayer(player);
        bar.setVisible(true);
        bar.setProgress(1.0);
        bossbars.put(id, bar);
    }

    public void removeBossBar(Player player) {
        UUID id = player.getUniqueId();
        BossBar bar = bossbars.remove(id);
        if (bar != null) {
            bar.removeAll();
            bar.setVisible(false);
        }
    }

    public BossBar getBossBar(Player player) {
        return bossbars.get(player.getUniqueId());
    }

    public void removeAllBossBars() {
        for (BossBar bar : bossbars.values()) {
            bar.removeAll();
            bar.setVisible(false);
        }
        bossbars.clear();
    }

    private void updateAllBossBars() {
        for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (plugin.getAutoBuyerManager().isAutobuyerEnabled(p)) {
                updateBossBar(p);
            }
        }
    }

    private void updateBossBar(Player player) {
        BossBar bar = bossbars.get(player.getUniqueId());
        if (bar != null) {
            try {
                String raw = plugin.getConfigManager().getBossbarText();
                String text = PlaceholderAPIHook.apply(raw, player);
                bar.setTitle(text);
            } catch (Exception ignored) {}
        }
    }

    public void shutdown() {
        plugin.getServer().getScheduler().cancelTask(updateTaskId);
        removeAllBossBars();
    }
}