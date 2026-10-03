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
    private final ShopLoader loader = new ShopLoader();
    private final Map<String, ShopFile> shops = new LinkedHashMap<>();
    private boolean enabled;

    public ShopManager(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
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

    public boolean hasTokens(@NotNull UUID uuid, int amount) {
        return plugin.getDataBase().getTokens(uuid) >= amount;
    }

    public void takeTokens(@NotNull UUID uuid, int amount) {
        plugin.getDataBase().addTokens(uuid, -amount);
    }
}
