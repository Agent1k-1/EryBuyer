package com.erydevs.bossbar;

import com.erydevs.EryBuyer;
import com.erydevs.papi.PlaceholderAPIHook;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Bossbars {

    private final EryBuyer plugin;
    private final Map<UUID, BossBar> bossbars = new ConcurrentHashMap<>();
    private final BarColor barColor;
    private int updateTaskId = -1;

    public Bossbars(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
        this.barColor = parseColor(plugin.getConfigManager().getBossbarColor());
        startBossBarUpdateTask();
    }

    private void startBossBarUpdateTask() {
        updateTaskId = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::updateAllBossBars, 100, 100);
    }

    public void createBossBar(@NotNull Player player) {
        UUID id = player.getUniqueId();
        if (bossbars.containsKey(id)) return;

        String text = PlaceholderAPIHook.apply(plugin.getConfigManager().getBossbarText(), player);
        BossBar bar = plugin.getServer().createBossBar(text, barColor, BarStyle.SOLID);
        bar.addPlayer(player);
        bar.setVisible(true);
        bar.setProgress(1.0);
        bossbars.put(id, bar);
    }

    public void removeBossBar(@NotNull Player player) {
        BossBar bar = bossbars.remove(player.getUniqueId());
        if (bar != null) {
            bar.removeAll();
            bar.setVisible(false);
        }
    }

    public void removeAllBossBars() {
        for (BossBar bar : bossbars.values()) {
            bar.removeAll();
            bar.setVisible(false);
        }
        bossbars.clear();
    }

    private void updateAllBossBars() {
        if (bossbars.isEmpty()) return;

        String rawText = plugin.getConfigManager().getBossbarText();
        for (Map.Entry<UUID, BossBar> entry : bossbars.entrySet()) {
            Player p = plugin.getServer().getPlayer(entry.getKey());
            if (p == null || !p.isOnline()) continue;
            entry.getValue().setTitle(PlaceholderAPIHook.apply(rawText, p));
        }
    }

    @NotNull
    private BarColor parseColor(@NotNull String colorName) {
        try {
            return BarColor.valueOf(colorName.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return BarColor.RED;
        }
    }

    public void shutdown() {
        plugin.getServer().getScheduler().cancelTask(updateTaskId);
        removeAllBossBars();
    }
}