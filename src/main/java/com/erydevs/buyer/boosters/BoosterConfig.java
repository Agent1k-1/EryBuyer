package com.erydevs.buyer.boosters;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

public class BoosterConfig {

    private static final String FILE_NAME = "booster.yml";
    private static final String SECTION = "booster-settings";

    private final JavaPlugin plugin;
    private final Map<Integer, BoosterLevel> levels = new TreeMap<>();

    private File file;
    private FileConfiguration configuration;

    public BoosterConfig(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void load() {
        levels.clear();

        file = new File(plugin.getDataFolder(), FILE_NAME);
        if (!file.exists()) plugin.saveResource(FILE_NAME, false);

        configuration = YamlConfiguration.loadConfiguration(file);
        if (!configuration.isConfigurationSection(SECTION)) return;

        ConfigurationSection section = configuration.getConfigurationSection(SECTION);
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            int level = parseLevel(key);
            if (level < 1) continue;

            double booster = section.getDouble(key + ".booster");
            double money = section.getDouble(key + ".money");
            int points = section.getInt(key + ".points");

            levels.put(level, new BoosterLevel(level, booster, money, points));
        }
    }

    public void reload() {
        load();
    }

    private int parseLevel(@NotNull String key) {
        int dash = key.indexOf('-');
        String numeric = dash > 0 ? key.substring(0, dash) : key;
        try {
            return Integer.parseInt(numeric);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @NotNull
    public Map<Integer, BoosterLevel> getLevels() {
        return Collections.unmodifiableMap(levels);
    }

    @Nullable
    public BoosterLevel getLevel(int level) {
        return levels.get(level);
    }

    public int getMaxLevel() {
        return levels.keySet().stream().mapToInt(Integer::intValue).max().orElse(0);
    }

    public double getBoosterMultiplier(int level) {
        BoosterLevel data = levels.get(level);
        return data != null ? data.getBooster() : 1.0;
    }

    public double getMoneyMultiplier(int level) {
        BoosterLevel data = levels.get(level);
        return data != null ? data.getMoney() : 1.0;
    }

    public int getPointsForLevel(int level) {
        BoosterLevel data = levels.get(level);
        return data != null ? data.getPoints() : 0;
    }
}
