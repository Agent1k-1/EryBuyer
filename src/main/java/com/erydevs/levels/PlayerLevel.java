package com.erydevs.levels;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class PlayerLevel {
    private final UUID uuid;
    private int currentLevel;
    private double totalEarned;

    public PlayerLevel(@NotNull UUID uuid, int currentLevel, double totalEarned) {
        this.uuid = uuid;
        this.currentLevel = currentLevel;
        this.totalEarned = totalEarned;
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int level) {
        this.currentLevel = level;
    }

    public double getTotalEarned() {
        return totalEarned;
    }

    public void setTotalEarned(double amount) {
        this.totalEarned = amount;
    }

    public void addEarnings(double amount) {
        this.totalEarned += amount;
    }
}
