package com.erydevs.gui.button;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ButtonConfig {

    private final String id;
    private final int slot;
    private final Material material;
    private final String materialStr;
    private final String name;
    private final List<String> lore;
    private final List<String> actions;
    private final double priceX1;
    private final double priceX64;
    private final int pointsX1;

    public ButtonConfig(@NotNull String id,
                        int slot,
                        @NotNull Material material,
                        @Nullable String materialStr,
                        @Nullable String name,
                        @Nullable List<String> lore,
                        @Nullable List<String> actions,
                        double priceX1,
                        double priceX64,
                        int pointsX1) {
        this.id = Objects.requireNonNull(id);
        this.slot = slot;
        this.material = Objects.requireNonNull(material);
        this.materialStr = materialStr;
        this.name = name == null ? "" : name;
        this.lore = lore == null ? Collections.emptyList() : lore;
        this.actions = actions == null ? Collections.emptyList() : actions;
        this.priceX1 = priceX1;
        this.priceX64 = priceX64;
        this.pointsX1 = pointsX1;
    }

    @NotNull
    public String getId() {
        return id;
    }

    public int getSlot() {
        return slot;
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    @Nullable
    public String getMaterialStr() {
        return materialStr;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<String> getLore() {
        return lore;
    }

    @NotNull
    public List<String> getActions() {
        return actions;
    }

    public double getPriceX1() {
        return priceX1;
    }

    public double getPriceX64() {
        return priceX64;
    }

    public int getPointsX1() {
        return pointsX1;
    }

    public boolean hasAction(@NotNull String fragment) {
        return actions.stream().anyMatch(a -> a != null && a.contains(fragment));
    }

}
