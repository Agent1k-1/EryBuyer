package com.erydevs.levels;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import java.util.HashMap;
import java.util.Map;

public class LevelConfig {

    private final JavaPlugin plugin;
    private final Map<Integer, LevelData> levels = new HashMap<>();

    public LevelConfig(JavaPlugin plugin) {
        this.plugin = plugin;
        loadLevels();
    }

    private void loadLevels() {
        FileConfiguration config = plugin.getConfig();
        if (!config.isConfigurationSection("levels")) return;
        for (String key : config.getConfigurationSection("levels").getKeys(false)) {
            try {
                int level = Integer.parseInt(key);
                double multiplier = config.getDouble("levels." + level + ".multiplier", 0.0);
                double required = config.getDouble("levels." + level + ".required-money", 0.0);
                levels.put(level, new LevelData(level, multiplier, required));
            } catch (NumberFormatException ignored) {}
        }
    }

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
