package com.erydevs.gui.entry;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class Entry {
    public final String id;
    public final Material material;
    public final String materialData;
    public final String name;
    public final List<String> lore;
    public final double priceX1;
    public final double priceX64;
    public final int pointsX1;
    public final int slot;

    public Entry(@NotNull String id, @NotNull Material material, @Nullable String name, @Nullable List<String> lore, double priceX1, double priceX64, int slot) {
        this(id, material, null, name, lore, priceX1, priceX64, 0, slot);
    }

    public Entry(@NotNull String id, @NotNull Material material, @Nullable String materialData, @Nullable String name, @Nullable List<String> lore, double priceX1, double priceX64, int slot) {
        this(id, material, materialData, name, lore, priceX1, priceX64, 0, slot);
    }

    public Entry(@NotNull String id, @NotNull Material material, @Nullable String materialData, @Nullable String name, @Nullable List<String> lore, double priceX1, double priceX64, int pointsX1, int slot) {
        this.id = id;
        this.material = material;
        this.materialData = materialData;
        this.name = name;
        this.lore = lore;
        this.priceX1 = priceX1;
        this.priceX64 = priceX64;
        this.pointsX1 = pointsX1;
        this.slot = slot;
    }
}
