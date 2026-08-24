package com.erydevs.buyer.autobuyer;

import com.erydevs.EryBuyer;
import com.erydevs.buyer.autobuyer.task.AutoBuyerTask;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AutoBuyerManager {

    private final EryBuyer plugin;
    private final Set<UUID> autobuyers = ConcurrentHashMap.newKeySet();
    private final AutoBuyerTask task;

    public AutoBuyerManager(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
        this.task = new AutoBuyerTask(plugin, this);
        this.task.start();
        startTopPointsUpdateTask();
    }

    private void startTopPointsUpdateTask() {
        long intervalTicks = Math.max(20L, plugin.getConfigManager().getBuyerTopUpdateInterval() * 20L);
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin,
                () -> plugin.getDataBase().refreshTopPointsCache(),
                intervalTicks, intervalTicks);
    }

    public void reload() {
        task.stop();
        task.start();
        for (UUID uuid : autobuyers) {
            task.markDirty(uuid);
        }
    }

    public void toggleAutobuyer(@NotNull Player player) {
        setAutobuyer(player, !isAutobuyerEnabled(player));
    }

    private void setAutobuyer(@NotNull Player player, boolean enabled) {
        UUID uuid = player.getUniqueId();

        if (enabled) {
            autobuyers.add(uuid);
            task.markDirty(uuid);
            if (plugin.getConfigManager().isBossbarEnabled() && plugin.getBossBarManager() != null) {
                plugin.getBossBarManager().createBossBar(player);
            }
        } else {
            autobuyers.remove(uuid);
            task.forget(uuid);
            if (plugin.getBossBarManager() != null) {
                plugin.getBossBarManager().removeBossBar(player);
            }
        }
    }

    public boolean isAutobuyerEnabled(@NotNull Player player) {
        return autobuyers.contains(player.getUniqueId());
    }

    public void markDirty(@NotNull Player player) {
        task.markDirty(player.getUniqueId());
    }

    public void removePlayer(@NotNull Player player) {
        UUID uuid = player.getUniqueId();
        autobuyers.remove(uuid);
        task.forget(uuid);

        if (plugin.getBossBarManager() != null) {
            plugin.getBossBarManager().removeBossBar(player);
        }

        plugin.getDataBase().evictPlayer(uuid);
    }

    public void shutdown() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            removePlayer(player);
        }

        task.stop();
        autobuyers.clear();
    }
}
