package com.erydevs.buyer.boosters;

public class BoosterLevel {

    private final int level;
    private final double booster;
    private final double money;
    private final int points;

    public BoosterLevel(int level, double booster, double money, int points) {
        this.level = level;
        this.booster = booster;
        this.money = money;
        this.points = points;
    }

    public int getLevel() {
        return level;
    }

    public double getBooster() {
        return booster;
    }

    public double getMoney() {
        return money;
    }

    public int getPoints() {
        return points;
    }
}
