package com.erydevs.buyer.best;

import com.erydevs.EryBuyer;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class BestConfig {

    private static final String MENU_FILE = "menu/bestmenu.yml";
    private static final String MENU_SETTINGS = "menu-settings";
    private static final String[] POOL_SECTION_CANDIDATES = {
            "item-settings.custom-items.available-materials",
            "tem-settings.custom-items.available-materials"
    };

    private final EryBuyer plugin;

    private String menuTitle = "";
    private int menuSize = 54;
    private List<Integer> slots = new ArrayList<>();
    private double minFactor = 1.0;
    private double maxFactor = 1.0;
    private String templateName = "";
    private List<String> templateLore = new ArrayList<>();
    private final Map<String, BestPoolEntry> pool = new LinkedHashMap<>();

    public BestConfig(@NotNull EryBuyer plugin) {
        this.plugin = plugin;
    }

    public void load() {
        pool.clear();
        slots = new ArrayList<>();
        templateLore = new ArrayList<>();

        File file = new File(plugin.getDataFolder(), MENU_FILE);
        if (!file.exists()) return;

        FileConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        menuTitle = cfg.getString("name");
        menuSize = cfg.getInt("size");

        loadTemplate(cfg);
        loadPool(cfg);
    }

    private void loadTemplate(@NotNull FileConfiguration cfg) {
        if (!cfg.isConfigurationSection(MENU_SETTINGS)) return;

        ConfigurationSection section = cfg.getConfigurationSection(MENU_SETTINGS);
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String path = MENU_SETTINGS + "." + key;
            if (!cfg.isList(path + ".slot")) continue;

            templateName = cfg.getString(path + ".name");
            templateLore = new ArrayList<>(cfg.getStringList(path + ".lore"));
            minFactor = cfg.getDouble(path + ".min-custom-item-factor");
            maxFactor = cfg.getDouble(path + ".max-custom-item-factor");

            List<Integer> slotList = new ArrayList<>();
            for (Object obj : cfg.getList(path + ".slot", Collections.emptyList())) {
                if (obj instanceof Number) slotList.add(((Number) obj).intValue());
            }
            slots = slotList;
            return;
        }
    }

    private void loadPool(@NotNull FileConfiguration cfg) {
        ConfigurationSection section = findPoolSection(cfg);
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            String path = key;
            String materialName = section.getString(path + ".material", key);
            double defaultPrice = section.getDouble(path + ".default-price");
            int limit = section.getInt(path + ".limit");
            int pointsX1 = section.getInt(path + ".points-from-the-buyer-x1");

            Material material = Material.matchMaterial(materialName);
            if (material == null) continue;

            pool.put(materialName, new BestPoolEntry(materialName, material, defaultPrice, limit, pointsX1));
        }
    }

    @Nullable
    private ConfigurationSection findPoolSection(@NotNull FileConfiguration cfg) {
        for (String candidate : POOL_SECTION_CANDIDATES) {
            if (cfg.isConfigurationSection(candidate)) {
                return cfg.getConfigurationSection(candidate);
            }
        }
        return null;
    }

    @Nullable
    public String getMenuTitle() {
        return menuTitle;
    }

    public int getMenuSize() {
        return menuSize;
    }

    @NotNull
    public List<Integer> getSlots() {
        return slots;
    }

    public double getMinFactor() {
        return minFactor;
    }

    public double getMaxFactor() {
        return maxFactor;
    }

    @Nullable
    public String getTemplateName() {
        return templateName;
    }

    @NotNull
    public List<String> getTemplateLore() {
        return templateLore;
    }

    @NotNull
    public Map<String, BestPoolEntry> getPool() {
        return pool;
    }
}
