package com.erydevs.api;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface BuyerAPI {

    int getBoosterLevel(@NotNull UUID uuid);

    long getPoints(@NotNull UUID uuid);

    double getMoneyMultiplier(@NotNull UUID uuid);

    double getBoosterMultiplier(@NotNull UUID uuid);

    boolean isAutobuyerEnabled(@NotNull Player player);

    void toggleAutobuyer(@NotNull Player player);

    @NotNull
    List<Map.Entry<String, Long>> getTopPoints();
}
