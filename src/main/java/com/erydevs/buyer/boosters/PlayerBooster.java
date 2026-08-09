package com.erydevs.buyer.boosters;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerBooster {

    private final UUID uuid;
    private int currentLevel;
    private long totalPoints;

    public PlayerBooster(@NotNull UUID uuid, int currentLevel, long totalPoints) {
        this.uuid = uuid;
        this.currentLevel = currentLevel;
        this.totalPoints = totalPoints;
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        this.currentLevel = currentLevel;
    }

    public long getTotalPoints() {
        return totalPoints;
    }

    public void setTotalPoints(long totalPoints) {
        this.totalPoints = totalPoints;
    }

    public void addPoints(long amount) {
        this.totalPoints += amount;
    }
}
