package com.erydevs.api;

import com.erydevs.api.addon.Addon;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface BuyerAPI {

    int getBoosterLevel(@NotNull UUID uuid);

    long getPoints(@NotNull UUID uuid);

    void addPoints(@NotNull Player player, long amount);

    double getMoneyMultiplier(@NotNull UUID uuid);

    double getBoosterMultiplier(@NotNull UUID uuid);

    boolean isAutobuyerEnabled(@NotNull Player player);

    void toggleAutobuyer(@NotNull Player player);

    @NotNull
    List<Map.Entry<String, Long>> getTopPoints();

    void registerAddon(@NotNull Addon addon);

    void unregisterAddon(@NotNull String name);

    @NotNull
    Collection<Addon> getAddons();
}
