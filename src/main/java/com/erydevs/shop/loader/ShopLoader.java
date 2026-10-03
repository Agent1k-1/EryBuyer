package com.erydevs.shop.loader;

import com.erydevs.gui.panel.PanelConfig;
import com.erydevs.gui.panel.PanelLoader;
import com.erydevs.shop.file.ShopFile;
import com.erydevs.shop.item.ShopItem;
import com.erydevs.shop.type.ShopItemType;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ShopLoader {

    @Nullable
    public ShopFile load(@NotNull File file) {
        if (!file.exists()) return null;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        String name = cfg.getString("name");
        int size = cfg.getInt("size");
        if (name == null) return null;

        Map<String, ShopItem> items = new LinkedHashMap<>();
        ConfigurationSection itemsSection = cfg.getConfigurationSection("items");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                ShopItem item = loadItem(key, itemsSection.getConfigurationSection(key));
                if (item != null) {
                    items.put(key, item);
                }
            }
        }

        List<PanelConfig> panels = PanelLoader.load(cfg);

        return new ShopFile(name, size, items, panels);
    }

    @Nullable
    private ShopItem loadItem(@NotNull String id, @Nullable ConfigurationSection section) {
        if (section == null) return null;

        Material material = Material.matchMaterial(section.getString("material"));
        if (material == null) return null;

        String name = section.getString("name", id);
        List<String> lore = section.getStringList("lore");
        int price = section.getInt("price");
        int slot = section.getInt("slot");
        double value = section.getDouble("value");
        long duration = section.getLong("duration-minutes");
        List<String> actions = section.getStringList("actions");

        ShopItemType type = null;
        String typeRaw = section.getString("type");
        if (typeRaw != null) {
            try {
                type = ShopItemType.valueOf(typeRaw);
            } catch (IllegalArgumentException ignored) {
            }
        }

        return new ShopItem(id, slot, material, name, lore, price, type, value, duration, actions);
    }
}
