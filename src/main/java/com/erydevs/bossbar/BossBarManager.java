package com.erydevs.bossbar;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import com.erydevs.EryBuyer;
import com.erydevs.placeholders.BuyerPlaceholder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BossBarManager {

    private final EryBuyer plugin;
    private final Map<UUID, BossBar> bossbars = new HashMap<>();

    public BossBarManager(EryBuyer plugin) {
        this.plugin = plugin;
    }

    public void createBossBar(Player player) {
        UUID id = player.getUniqueId();
        if (bossbars.containsKey(id)) return;
        String raw = plugin.getConfigManager().getConfig().getString("bossbar-settings.text", "&fУ вас включён &cавто-скупщик");
        String text = BuyerPlaceholder.apply(raw, player);
        BarColor color = BarColor.RED;
        try {
            String c = plugin.getConfigManager().getConfig().getString("bossbar-settings.color", "RED");
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
}