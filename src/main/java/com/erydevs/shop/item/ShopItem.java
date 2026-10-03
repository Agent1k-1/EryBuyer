package com.erydevs.shop.item;

import com.erydevs.shop.type.ShopItemType;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ShopItem {

    private final String id;
    private final int slot;
    private final Material material;
    private final String name;
    private final List<String> lore;
    private final int price;
    private final ShopItemType type;
    private final double value;
    private final long durationMinutes;
    private final List<String> actions;

    public ShopItem(@NotNull String id, int slot, @NotNull Material material, @NotNull String name,
                     @NotNull List<String> lore, int price, @Nullable ShopItemType type,
                     double value, long durationMinutes, @NotNull List<String> actions) {
        this.id = id;
        this.slot = slot;
        this.material = material;
        this.name = name;
        this.lore = lore;
        this.price = price;
        this.type = type;
        this.value = value;
        this.durationMinutes = durationMinutes;
        this.actions = actions;
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

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public List<String> getLore() {
        return lore;
    }

    public int getPrice() {
        return price;
    }

    @Nullable
    public ShopItemType getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    @NotNull
    public List<String> getActions() {
        return actions;
    }
}
