package com.erydevs.gui.menu;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MenuLoad {

    private final JavaPlugin plugin;
    private final Map<String, FileConfiguration> configCache = new HashMap<>();
    private final Map<String, Long> cacheTime = new HashMap<>();
    private static final long CACHE_DURATION = 5000;

    public MenuLoad(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public FileConfiguration getMenuConfig(String name) {
        if (name == null || name.isEmpty()) {
            return new YamlConfiguration();
        }

        long currentTime = System.currentTimeMillis();
        if (configCache.containsKey(name)) {
            Long lastLoad = cacheTime.get(name);
            if (lastLoad != null && (currentTime - lastLoad) < CACHE_DURATION) {
                return configCache.get(name);
            }
        }

        File file = new File(plugin.getDataFolder(), "menu/" + name + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        configCache.put(name, config);
        cacheTime.put(name, currentTime);
        return config;
    }

    public void clearCache() {
        configCache.clear();
        cacheTime.clear();
    }

    public void clearCache(String name) {
        configCache.remove(name);
        cacheTime.remove(name);
    }

    public int getCacheSize() {
        return configCache.size();
    }
}
