package com.erydevs.gui;

import org.bukkit.Material;
import java.util.List;

public class Entry {
    public final String id;
    public final Material material;
    public final String name;
    public final List<String> lore;
    public final double priceX1;
    public final double priceX64;
    public final int slot;

    public Entry(String id, Material material, String name, List<String> lore, double priceX1, double priceX64, int slot) {
        this.id = id;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.priceX1 = priceX1;
        this.priceX64 = priceX64;
        this.slot = slot;
    }
}