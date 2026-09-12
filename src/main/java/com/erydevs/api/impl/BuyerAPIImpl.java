package com.erydevs.api.impl;

import com.erydevs.EryBuyer;
import com.erydevs.api.BuyerAPI;
import com.erydevs.buyer.boosters.PlayerBooster;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BuyerAPIImpl implements BuyerAPI {

    private final EryBuyer plugin;

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
}
