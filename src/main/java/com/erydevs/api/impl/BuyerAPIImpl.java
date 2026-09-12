package com.erydevs.api.impl;

import com.erydevs.EryBuyer;
import com.erydevs.api.BuyerAPI;
import com.erydevs.api.addon.Addon;
import com.erydevs.buyer.boosters.PlayerBooster;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

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
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin,
                () -> plugin.getDataBase().save(booster));
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
