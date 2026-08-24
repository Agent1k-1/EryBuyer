package com.erydevs.gui.panel;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PanelConfig {

    private final Material material;
    private final String materialData;
    private final String name;
    private final List<String> lore;
    private final List<Integer> slots;

    public PanelConfig(@NotNull Material material, @Nullable String materialData,
                       @Nullable String name, @NotNull List<String> lore, @NotNull List<Integer> slots) {
        this.material = material;
        this.materialData = materialData;
        this.name = name;
        this.lore = lore;
        this.slots = slots;
    }

    @NotNull
    public Material getMaterial() {
        return material;
    }

    @Nullable
    public String getMaterialData() {
        return materialData;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @NotNull
    public List<String> getLore() {
        return lore;
    }

    @NotNull
    public List<Integer> getSlots() {
        return slots;
    }
}
