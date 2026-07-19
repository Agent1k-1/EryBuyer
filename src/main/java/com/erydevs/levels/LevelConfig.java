package com.erydevs.levels;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LevelConfig {

    private final JavaPlugin plugin;
    private final Map<Integer, LevelData> levels = new HashMap<>();
    private File levelFile;
    private FileConfiguration levelConfiguration;

    public LevelConfig(@NotNull JavaPlugin plugin) {
        this.plugin = plugin;
        loadLevels();
    }

    private void loadLevels() {
        levels.clear();

        levelFile = new File(plugin.getDataFolder(), "level.yml");
        if (!levelFile.exists()) {
            plugin.saveResource("level.yml", false);
        }

        levelConfiguration = YamlConfiguration.loadConfiguration(levelFile);

        if (!levelConfiguration.isConfigurationSection("levels")) return;

        for (String key : levelConfiguration.getConfigurationSection("levels").getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                double multiplier = levelConfiguration.getDouble("levels." + level + ".multiplier");
                double required = levelConfiguration.getDouble("levels." + level + ".required-money");
                levels.put(level, new LevelData(level, multiplier, required));
            } catch (NumberFormatException ignored) {}
        }
    }

    public void reloadLevels() {
        loadLevels();
    }

    @Nullable
    public LevelData getLevelData(int level) {
        return levels.get(level);
    }

    public double getMultiplierByLevel(int level) {
        LevelData data = levels.get(level);
        return data != null ? data.multiplier : 0.0;
    }

    public int getLevelByMoney(double money) {
        int maxLevel = 1;
        for (Map.Entry<Integer, LevelData> entry : levels.entrySet()) {
            if (entry.getValue().requiredMoney <= money) {
                maxLevel = Math.max(maxLevel, entry.getKey());
            }
        }
        return maxLevel;
    }

    public int getMaxLevel() {
        return levels.keySet().stream().mapToInt(Integer::intValue).max().orElse(1);
    }

    public double getRequiredMoneyForLevel(int level) {
        LevelData data = levels.get(level);
        return data != null ? data.requiredMoney : 0.0;
    }
}
