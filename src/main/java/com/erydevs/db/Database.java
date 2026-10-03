package com.erydevs.db;

import com.erydevs.buyer.boosters.PlayerBooster;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface Database {

    boolean isConnected();

    int getSoldAmount(@NotNull UUID uuid, @NotNull String material);

    void addSoldAmount(@NotNull UUID uuid, @NotNull String material, int amount);

    void resetAllLimits();

    void resetLimitsForMaterial(@NotNull String material);

    @NotNull
    PlayerBooster getPlayerData(@NotNull UUID uuid);

    void save(@NotNull PlayerBooster booster);

    void evictPlayer(@NotNull UUID uuid);

    @NotNull
    List<Map.Entry<String, Long>> getTopPoints();

    void refreshTopPointsCache();

    int getTokens(@NotNull UUID uuid);

    void addTokens(@NotNull UUID uuid, int amount);

    void closeConnection();
}
