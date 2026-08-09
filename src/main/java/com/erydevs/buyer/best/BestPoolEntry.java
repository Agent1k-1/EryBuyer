package com.erydevs.buyer.best;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class BestPoolEntry {

    private final String materialName;
    private final Material material;
    private final double defaultPrice;
    private final int limit;
    private final int pointsX1;

    public BestPoolEntry(@NotNull String materialName, @NotNull Material material, double defaultPrice, int limit, int pointsX1) {
        this.materialName = materialName;
        this.material = material;
        this.defaultPrice = defaultPrice;
        this.limit = limit;
        this.pointsX1 = pointsX1;
    }

    @NotNull
    public String getMaterialName() {
        return materialName;
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    public double getDefaultPrice() {
        return defaultPrice;
    }

    public int getLimit() {
        return limit;
    }

    public int getPointsX1() {
        return pointsX1;
    }
}
