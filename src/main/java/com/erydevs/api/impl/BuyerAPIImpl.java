package com.erydevs.api.impl;

import com.erydevs.EryBuyer;
import com.erydevs.api.BuyerAPI;
import com.erydevs.api.addon.Addon;
import com.erydevs.buyer.boosters.PlayerBooster;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BuyerAPIImpl implements BuyerAPI {

    private final EryBuyer plugin;
    private final Map<String, Addon> addons = new ConcurrentHashMap<>();

    public BuyerAPIImpl(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
    }

    @Override
    public int getBoosterLevel(@NotNull UUID uuid) {
        return plugin.getDataBase().getPlayerData(uuid).getCurrentLevel();
    }

    @Override
    public long getPoints(@NotNull UUID uuid) {
        return plugin.getDataBase().getPlayerData(uuid).getTotalPoints();
    }

    @Override
    public void addPoints(@NotNull Player player, long amount) {
        PlayerBooster booster = plugin.getDataBase().getPlayerData(player.getUniqueId());
        plugin.getBoosterManager().addPointsAndCheckLevelUp(player, booster, amount);
        plugin.getScheduler().runAsync(() -> plugin.getDataBase().save(booster));
    }

    @Override
    public double getMoneyMultiplier(@NotNull UUID uuid) {
        PlayerBooster booster = plugin.getDataBase().getPlayerData(uuid);
        return plugin.getBoosterManager().getMoneyMultiplier(booster);
    }

    @Override
    public double getBoosterMultiplier(@NotNull UUID uuid) {
        PlayerBooster booster = plugin.getDataBase().getPlayerData(uuid);
        return plugin.getBoosterManager().getBoosterMultiplier(booster);
    }

    @Override
    public boolean isAutobuyerEnabled(@NotNull Player player) {
        return plugin.getAutoBuyerManager().isAutobuyerEnabled(player);
    }

    @Override
    public void toggleAutobuyer(@NotNull Player player) {
        plugin.getAutoBuyerManager().toggleAutobuyer(player);
    }

    @Override
    public CompletableFuture<Void> loadAutobuyerSettings(UUID uuid) {
        return plugin.getAutoBuyerSettings().load(uuid);
    }

    @Override
    public boolean isAutobuyerSettingsLoaded(UUID uuid) {
        return plugin.getAutoBuyerSettings().isLoaded(uuid);
    }

    @Override
    public boolean isAutobuyerItemEnabled(UUID uuid, Material material) {
        return plugin.getAutoBuyerSettings().isItemEnabled(uuid, material);
    }

    @Override
    public void setAutobuyerItemEnabled(Player player, Material material, boolean enabled) {
        plugin.getAutoBuyerSettings().setItemEnabled(player.getUniqueId(), material, enabled);
        plugin.getAutoBuyerManager().markDirty(player);
    }

    @Override
    public void openAutobuyerMenu(Player player) {
        plugin.getScheduler().runForPlayer(player, () -> player.openInventory(plugin.getBuyerGUI().createInventory(player, "autobuyer")));
    }

    @Override
    @NotNull
    public List<Map.Entry<String, Long>> getTopPoints() {
        return plugin.getDataBase().getTopPoints();
    }

    @Override
    public void registerAddon(@NotNull Addon addon) {
        addons.put(addon.getName(), addon);
        plugin.getLogger().info("Зарегистрирован аддон: " + addon.getName() + " v" + addon.getVersion());
    }

    @Override
    public void unregisterAddon(@NotNull String name) {
        if (addons.remove(name) != null) {
            plugin.getLogger().info("Аддон отключен: " + name);
        }
    }

    @Override
    @NotNull
    public Collection<Addon> getAddons() {
        return addons.values();
    }
}