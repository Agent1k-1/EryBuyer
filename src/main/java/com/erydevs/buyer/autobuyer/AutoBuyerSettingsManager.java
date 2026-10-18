package com.erydevs.buyer.autobuyer;

import com.erydevs.EryBuyer;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import org.bukkit.Material;

public final class AutoBuyerSettingsManager {
    private final EryBuyer plugin;
    private final Map<UUID, CompletableFuture<Map<Material, Boolean>>> cache = new ConcurrentHashMap<>();
    private final Map<UUID, Map<Material, SaveRequest>> pending = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "EryBuyer-autobuyer-settings"));

    public AutoBuyerSettingsManager(EryBuyer plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<Void> load(UUID uuid) {
        CompletableFuture<Map<Material, Boolean>> future = cache.computeIfAbsent(uuid, key ->
                CompletableFuture.supplyAsync(() -> {
                    Map<Material, Boolean> result = new ConcurrentHashMap<>();
                    plugin.getDataBase().loadAutoBuyerSettings(key).forEach((name, enabled) -> {
                        Material material = Material.matchMaterial(name);
                        if (material != null) result.put(material, enabled);
                    });
                    return result;
                }, executor));
        return future.handle((result, error) -> {
            if (error != null) {
                cache.remove(uuid, future);
                throw new CompletionException(error);
            }
            return null;
        });
    }

    public void runForPlayer(org.bukkit.entity.Player player, Runnable action) {
        load(player.getUniqueId()).whenComplete((ignored, error) -> {
            if (!plugin.isEnabled()) return;
            plugin.getScheduler().runForPlayer(player, () -> {
                if (!player.isOnline()) return;
                if (error != null) {
                    plugin.getLogger().log(Level.WARNING, "Не удалось загрузить настройки автоскупщика", error);
                    player.sendMessage(com.erydevs.utils.HexUtils.colorize("&cНе удалось загрузить настройки. Попробуйте снова."));
                    return;
                }
                action.run();
            });
        });
    }

    public void setAllItemsEnabled(UUID uuid, boolean enabled) {
        if (!isLoaded(uuid)) throw new IllegalStateException("Autobuyer settings are not loaded: " + uuid);
        Set<Material> materials = new HashSet<>(cache.get(uuid).join().keySet());
        plugin.getBuyerGUI().getBuyableEntries().forEach(entry -> {
            if (entry.priceX1 > 0 && entry.material != null) materials.add(entry.material);
        });
        if (plugin.getBestManager() != null) {
            plugin.getBestManager().getBestConfig().getPool().values().forEach(entry -> materials.add(entry.getMaterial()));
        }
        materials.forEach(material -> setItemEnabled(uuid, material, enabled));
    }

    public boolean isLoaded(UUID uuid) {
        CompletableFuture<Map<Material, Boolean>> future = cache.get(uuid);
        return future != null && future.isDone() && !future.isCompletedExceptionally();
    }

    public boolean isItemEnabled(UUID uuid, Material material) {
        if (!isLoaded(uuid)) return false;
        return cache.get(uuid).join().getOrDefault(material, true);
    }

    public void setItemEnabled(UUID uuid, Material material, boolean enabled) {
        if (!isLoaded(uuid)) throw new IllegalStateException("Autobuyer settings are not loaded: " + uuid);
        cache.get(uuid).join().put(material, enabled);
        SaveRequest request = new SaveRequest(enabled);
        pending.computeIfAbsent(uuid, key -> new ConcurrentHashMap<>()).put(material, request);
        executor.execute(() -> persist(uuid, material, request));
    }

    private void persist(UUID uuid, Material material, SaveRequest request) {
        try {
            plugin.getDataBase().saveAutoBuyerSetting(uuid, material.name(), request.enabled);
            pending.get(uuid).remove(material, request);
        } catch (RuntimeException e) {
            plugin.getLogger().log(Level.SEVERE, "Не удалось сохранить настройки автоскупщика: " + uuid, e);
        }
    }

    public synchronized void close() {
        if (executor.isShutdown()) return;
        executor.execute(() -> pending.forEach((uuid, values) ->
                values.forEach((material, enabled) -> persist(uuid, material, enabled))));
        executor.shutdown();
        try {
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                plugin.getLogger().severe("Не завершено сохранение настроек автоскупщика");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        cache.clear();
    }

    private static final class SaveRequest {
        private final boolean enabled;

        private SaveRequest(boolean enabled) {
            this.enabled = enabled;
        }
    }
}
