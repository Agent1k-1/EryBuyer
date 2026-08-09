package com.erydevs.buyer.best;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class BestItem {

    private final String materialName;
    private final Material material;
    private final double defaultPrice;
    private final int limit;
    private final double customPrice;
    private final int pointsX1;
    private final int slot;

    public BestItem(@NotNull String materialName, @NotNull Material material, double defaultPrice, int limit, double customPrice, int pointsX1, int slot) {
        this.materialName = materialName;
        this.material = material;
        this.defaultPrice = defaultPrice;
        this.limit = limit;
        this.customPrice = customPrice;
        this.pointsX1 = pointsX1;
        this.slot = slot;
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

    public double getDefaultPrice64() {
        return defaultPrice * 64;
    }

    public int getLimit() {
        return limit;
    }

    public double getCustomPrice() {
        return customPrice;
    }

    public double getCustomPrice64() {
        return customPrice * 64;
    }

    public int getPointsX1() {
        return pointsX1;
    }

    public int getSlot() {
        return slot;
    }
}
