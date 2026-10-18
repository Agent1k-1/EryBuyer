package com.erydevs.shop;

import com.erydevs.EryBuyer;
import com.erydevs.shop.file.ShopFile;
import com.erydevs.shop.loader.ShopLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class ShopManager {

    private final EryBuyer plugin;
    private final java.util.function.LongSupplier clock;
    private final ShopLoader loader = new ShopLoader();
    private final Map<String, ShopFile> shops = new LinkedHashMap<>();
    private final Map<UUID, PurchasedBooster> purchasedBoosters = new java.util.concurrent.ConcurrentHashMap<>();
    private boolean enabled;

    public ShopManager(@NotNull EryBuyer plugin) {
        this(plugin, System::currentTimeMillis);
    }

    ShopManager(EryBuyer plugin, java.util.function.LongSupplier clock) {
        this.plugin = plugin;
        this.clock = clock;
    }

    public void enable() {
        enabled = plugin.getConfigManager().isBuyerShopEnabled();
        if (!enabled) return;

        load();
    }

    public void reload() {
        enabled = plugin.getConfigManager().isBuyerShopEnabled();
        if (enabled) {
            load();
        } else {
            shops.clear();
        }
    }

    private void load() {
        shops.clear();
        for (String path : plugin.getConfigManager().getShopRegister()) {
            if (path == null || path.trim().isEmpty()) continue;

            File file = new File(plugin.getDataFolder(), path);
            if (!file.exists()) plugin.saveResource(path, false);

            ShopFile shopFile = loader.load(file);
            if (shopFile != null) {
                shops.put(path, shopFile);
            }
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Nullable
    public ShopFile getShop(@NotNull String path) {
        return shops.get(path);
    }

    @Nullable
    public String getFirstShopPath() {
        return shops.isEmpty() ? null : shops.keySet().iterator().next();
    }

    public void rememberPurchasedBooster(UUID uuid, com.erydevs.shop.item.ShopItem item) {
        long duration = java.util.concurrent.TimeUnit.MINUTES.toMillis(item.getDurationMinutes());
        if (duration <= 0) return;
        long now = clock.getAsLong();
        long expiresAt = duration > Long.MAX_VALUE - now ? Long.MAX_VALUE : now + duration;
        purchasedBoosters.put(uuid, new PurchasedBooster(item.getId(), item.getName(), expiresAt));
    }

    @Nullable
    private PurchasedBooster getPurchasedBooster(UUID uuid) {
        PurchasedBooster booster = purchasedBoosters.get(uuid);
        if (booster != null && booster.expiresAt <= clock.getAsLong()) {
            purchasedBoosters.remove(uuid, booster);
            return null;
        }
        return booster;
    }

    @Nullable
    public String getPurchasedBoosterName(UUID uuid) {
        PurchasedBooster booster = getPurchasedBooster(uuid);
        return booster != null ? booster.name : null;
    }

    public boolean hasPurchasedBooster(UUID uuid, String itemId) {
        PurchasedBooster booster = getPurchasedBooster(uuid);
        return booster != null && booster.id.equals(itemId);
    }

    public long getRemainingBoosterSeconds(UUID uuid) {
        PurchasedBooster booster = getPurchasedBooster(uuid);
        if (booster == null) return 0;
        long remaining = Math.max(0, booster.expiresAt - clock.getAsLong());
        return remaining / 1000 + (remaining % 1000 == 0 ? 0 : 1);
    }

    private static final class PurchasedBooster {
        private final String id;
        private final String name;
        private final long expiresAt;

        private PurchasedBooster(String id, String name, long expiresAt) {
            this.id = id;
            this.name = name;
            this.expiresAt = expiresAt;
        }
    }

    public boolean hasTokens(@NotNull UUID uuid, int amount) {
        return plugin.getDataBase().getTokens(uuid) >= amount;
    }

    public void takeTokens(@NotNull UUID uuid, int amount) {
        plugin.getDataBase().addTokens(uuid, -amount);
    }
}
