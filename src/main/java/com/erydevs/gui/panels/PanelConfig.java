package com.erydevs.gui.panels;

import org.bukkit.Material;
import java.util.List;

public class PanelConfig {
    private final Material material;
    private final String headTextureBase64;
    private final String name;
    private final List<String> lore;
    private final List<Integer> slots;

    public PanelConfig(Material material, String name,
                       List<String> lore, List<Integer> slots) {
        this(material, null, name, lore, slots);
    }

    public PanelConfig(Material material, String headTextureBase64, String name,
                       List<String> lore, List<Integer> slots) {
        this.material = material;
        this.headTextureBase64 = headTextureBase64;
        this.name = name;
        this.lore = lore;
        this.slots = slots;
    }

    public Material getMaterial() {
        return material;
    }

    public String getHeadTextureBase64() {
        return headTextureBase64;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<Integer> getSlots() {
        return slots;
    }
}
