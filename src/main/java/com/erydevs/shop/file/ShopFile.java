package com.erydevs.shop.file;

import com.erydevs.gui.panel.PanelConfig;
import com.erydevs.shop.item.ShopItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public class ShopFile {

    private final String name;
    private final int size;
    private final Map<String, ShopItem> items;
    private final List<PanelConfig> panels;

    public ShopFile(@NotNull String name, int size, @NotNull Map<String, ShopItem> items, @NotNull List<PanelConfig> panels) {
        this.name = name;
        this.size = size;
        this.items = items;
        this.panels = panels;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public int getSize() {
        return size;
    }

    @NotNull
    public Map<String, ShopItem> getItems() {
        return items;
    }

    @NotNull
    public List<PanelConfig> getPanels() {
        return panels;
    }
}
