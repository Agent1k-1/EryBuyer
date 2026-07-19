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

    public ButtonConfig(@NotNull String id,
                        int slot,
                        @NotNull Material material,
                        @Nullable String name,
                        @Nullable List<String> lore,
                        @Nullable List<String> actions) {
        this(id, slot, material, null, name, lore, actions);
    }

    public ButtonConfig(@NotNull String id,
                        int slot,
                        @NotNull Material material,
                        @Nullable String materialStr,
                        @Nullable String name,
                        @Nullable List<String> lore,
                        @Nullable List<String> actions) {
        this.id = Objects.requireNonNull(id);
        this.slot = slot;
        this.material = Objects.requireNonNull(material);
        this.materialStr = materialStr;
        this.name = name == null ? "" : name;
        this.lore = lore == null ? Collections.emptyList() : lore;
        this.actions = actions == null ? Collections.emptyList() : actions;
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

    public boolean hasAction(@NotNull String fragment) {
        return actions.stream().anyMatch(a -> a != null && a.contains(fragment));
    }

}