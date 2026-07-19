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

    public Bossbars(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
        this.barColor = resolveColor(plugin.getConfigManager().getBossbarColor());
        plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this::updateAll, 100, 100);
    }

    public void createBossBar(@NotNull Player player) {
        UUID id = player.getUniqueId();
        if (bossbars.containsKey(id)) return;

        String text = PlaceholderAPIHook.apply(plugin.getConfigManager().getBossbarText(), player);
        BossBar bar = plugin.getServer().createBossBar(text, barColor, BarStyle.SOLID);
        bar.addPlayer(player);
        bar.setProgress(1.0);
        bossbars.put(id, bar);
    }

    public void removeBossBar(@NotNull Player player) {
        BossBar bar = bossbars.remove(player.getUniqueId());
        if (bar != null) bar.removeAll();
    }

    public void shutdown() {
        bossbars.values().forEach(BossBar::removeAll);
        bossbars.clear();
    }

    private void updateAll() {
        if (bossbars.isEmpty()) return;
        String rawText = plugin.getConfigManager().getBossbarText();
        for (Map.Entry<UUID, BossBar> e : bossbars.entrySet()) {
            Player p = plugin.getServer().getPlayer(e.getKey());
            if (p != null && p.isOnline()) e.getValue().setTitle(PlaceholderAPIHook.apply(rawText, p));
        }
    }

    @NotNull
    private static BarColor resolveColor(@NotNull String name) {
        try {
            return BarColor.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return BarColor.RED;
        }
    }
}
