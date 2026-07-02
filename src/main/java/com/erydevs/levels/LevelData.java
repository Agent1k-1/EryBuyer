package com.erydevs.levels;

public class LevelData {
    public final int level;
    public final double multiplier;
    public final double requiredMoney;

    public LevelData(int level, double multiplier, double requiredMoney) {
        this.level = level;
        this.multiplier = multiplier;
        this.requiredMoney = requiredMoney;
    }
}
