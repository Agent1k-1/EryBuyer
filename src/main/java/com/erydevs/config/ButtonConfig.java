package com.erydevs.config;

import org.bukkit.Material;
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

    public ButtonConfig(String id,
                        int slot,
                        Material material,
                        String name,
                        List<String> lore,
                        List<String> actions) {
        this(id, slot, material, null, name, lore, actions);
    }

    public ButtonConfig(String id,
                        int slot,
                        Material material,
                        String materialStr,
                        String name,
                        List<String> lore,
                        List<String> actions) {
        this.id = Objects.requireNonNull(id);
        this.slot = slot;
        this.material = Objects.requireNonNull(material);
        this.materialStr = materialStr;
        this.name = name == null ? "" : name;
        this.lore = lore == null ? Collections.emptyList() : lore;
        this.actions = actions == null ? Collections.emptyList() : actions;
    }

    public String getId() {
        return id;
    }

    public int getSlot() {
        return slot;
    }

    public Material getMaterial() {
        return material;
    }

    public String getMaterialStr() {
        return materialStr;
    }

    public String getName() {
        return name;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getActions() {
        return actions;
    }

    public boolean hasAction(String fragment) {
        return actions.stream().anyMatch(a -> a != null && a.contains(fragment));
    }

    @Override
    public String toString() {
        return "ButtonConfig{" +
                "id='" + id + '\'' +
                ", slot=" + slot +
                ", material=" + material +
                ", name='" + name + '\'' +
                '}';
    }
}
